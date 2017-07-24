package workshop.part1;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import javaslang.collection.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import workshop.ad.Ad;
import workshop.fraudwordsservice.FraudWord;
import workshop.fraudwordsservice.FraudWordService;
import workshop.part1.VettingActor.GetNumVettedAds;
import workshop.part1.VettingActor.NumVettedAds;
import workshop.part1.VettingActor.ReportNumVettedAds;
import workshop.userservice.UserCriminalRecord;
import workshop.userservice.UserService;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

        assertThat(verdict.verdictStatus, is(VerdictStatus.GOOD));
    }

    @Test
    public void doesNotAcceptAdWithFraudWords() {
        when(userService.vettUser(eq(1)))
            .thenReturn(UserCriminalRecord.GOOD);

        when(fraudWordService.examineWords(any()))
            .thenReturn(List.of(new FraudWord("nigeria")));

        createVettingActor().tell(createAd(), sender.ref());
        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict.verdictStatus, is(VerdictStatus.BAD));
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

        assertThat(verdict.verdictStatus, is(VerdictStatus.BAD));
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

    private Ad createAd() {
        return new Ad(1, "Sofa", "Selling sofa");
    }

    private TestActorRef<VettingActor> createVettingActor() {
        return createVettingActor(TestProbe.apply(system).ref(), Duration.create(1, TimeUnit.SECONDS));
    }

    private TestActorRef<VettingActor> createVettingActor(ActorRef numVettedAdsActor, FiniteDuration numVettedAdsInterval) {
        return TestActorRef.create(system, Props.create(VettingActor.class, () -> new VettingActor(userService, fraudWordService, numVettedAdsActor, numVettedAdsInterval)));
    }
}