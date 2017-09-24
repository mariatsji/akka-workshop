package workshop.part1;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import javaslang.collection.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.BoxedUnit;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserCriminalRecord;
import workshop.common.userservice.UserService;
import workshop.part1.VettingActor.GetNumVettedAds;
import workshop.part1.VettingActor.NumVettedAds;
import workshop.part1.VettingActor.ReportNumVettedAds;

import static akka.actor.SupervisorStrategy.resume;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class VettingActorTest extends AkkaTest {

    @Mock
    UserService userService;
    @Mock
    FraudWordService fraudWordService;

    @Test
    public void acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.GOOD);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.empty());

        Ad ad = createAd();
        createVettingActor().tell(ad, sender.ref());
        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict, is(Verdict.GOOD));
    }

    @Test
    public void doesNotAcceptAdWithFraudWords() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.GOOD);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.of(new FraudWord("nigeria")));

        createVettingActor().tell(createAd(), sender.ref());
        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict, is(Verdict.BAD));
    }

    @Test
    public void doesNotAcceptAdWithUserHavingCriminalRecord() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.EVIL);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.empty());

        Ad ad = createAd();
        createVettingActor().tell(ad, sender.ref());
        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict, is(Verdict.BAD));
    }
    
    @Test
    public void respondsWithNumberOfVettedAds() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.EVIL);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.empty());

        TestActorRef<VettingActor> vettingActor = createVettingActor();
        sender.send(vettingActor, new GetNumVettedAds());
        assertThat(sender.expectMsgClass(NumVettedAds.class).numVettedAds, is(0));

        sender.send(vettingActor, createAd());
        sender.expectMsgClass(Verdict.class);

        sender.send(vettingActor, new GetNumVettedAds());
        assertThat(sender.expectMsgClass(NumVettedAds.class).numVettedAds, is(1));
    }

    @Test
    public void reportsNumVettedAdsEveryConfiguredInterval() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.GOOD);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.empty());

        createVettingActor(sender.ref(), Duration.create(1, TimeUnit.MILLISECONDS));

        assertThat(sender.expectMsgClass(NumVettedAds.class).numVettedAds, is(0));
        assertThat(sender.expectMsgClass(NumVettedAds.class).numVettedAds, is(0));
        assertThat(sender.expectMsgClass(NumVettedAds.class).numVettedAds, is(0));
    }

    @Test
    public void reportsNumVettedAds() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.GOOD);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.empty());

        TestProbe numVettedAdsActor = TestProbe.apply(system);
        TestActorRef<VettingActor> vettingActor = createVettingActor(numVettedAdsActor.ref(), Duration.create(24, TimeUnit.HOURS));

        sender.send(vettingActor, new ReportNumVettedAds());
        assertThat(numVettedAdsActor.expectMsgClass(NumVettedAds.class).numVettedAds, is(0));

        sender.send(vettingActor, createAd());
        sender.send(vettingActor, new ReportNumVettedAds());
        assertThat(numVettedAdsActor.expectMsgClass(NumVettedAds.class).numVettedAds, is(1));

        sender.send(vettingActor, createAd());
        sender.send(vettingActor, new ReportNumVettedAds());
        assertThat(numVettedAdsActor.expectMsgClass(NumVettedAds.class).numVettedAds, is(2));
    }

    @Test
    public void doesNotCountVettingThatFails() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.GOOD);

        doThrow(new RuntimeException("Vetting failed"))
            .when(userService)
            .vettUser(eq(2));

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.empty());

        TestActorRef<VettingActor> vettingActor = createVettingActor();
        sender.send(vettingActor, new Ad(1, "Sofa", "Selling sofa"));
        sender.expectMsgClass(Verdict.class);
        sender.send(vettingActor, new Ad(2, "Sofa", "Selling sofa"));

        sender.send(vettingActor, new GetNumVettedAds());
        assertThat(sender.expectMsgClass(NumVettedAds.class).numVettedAds, is(1));
    }

    private Ad createAd() {
        return new Ad(1, "Sofa", "Selling sofa");
    }

    private TestActorRef<VettingActor> createVettingActor() {
        return createVettingActor(TestProbe.apply(system).ref(), Duration.create(1, TimeUnit.SECONDS));
    }

    private TestActorRef<VettingActor> createVettingActor(ActorRef numVettedAdsActor, FiniteDuration numVettedAdsInterval) {
        TestActorRef<Actor> supervisor = TestActorRef.create(system, Props.create(ResumingSupervisor.class));

        return TestActorRef.create(system, Props.create(VettingActor.class,
            () -> new VettingActor(userService, fraudWordService, numVettedAdsActor, numVettedAdsInterval)), supervisor
        );
    }

    private static class ResumingSupervisor extends AbstractActor {
        @Override
        public SupervisorStrategy supervisorStrategy() {
            return new OneForOneStrategy(DeciderBuilder.matchAny(e -> resume()).build());
        }

        @Override
        public PartialFunction<Object, BoxedUnit> receive() {
            return ReceiveBuilder.create().build();
        }
    }

}