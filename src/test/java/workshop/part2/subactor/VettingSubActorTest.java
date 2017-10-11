package workshop.part2.subactor;

import java.util.concurrent.TimeUnit;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import javaslang.collection.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.AkkaTest;
import workshop.part1.Verdict;
import workshop.part2.FraudWordActor;
import workshop.part2.FraudWordActor.ExamineWordsResult;
import workshop.part2.UserActor.CheckUser;
import workshop.part2.UserActor.CheckUserResult;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VettingSubActorTest extends AkkaTest {

    TestProbe userActor = TestProbe.apply(system);
    TestProbe fraudWordActor = TestProbe.apply(system);

    @Test
    public void acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        Verdict.VerdictType verdict = sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);

        assertThat(verdict, is(Verdict.VerdictType.GOOD));
    }

    @Test
    public void doesNotAcceptAdWithFraudWords() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.of(new FraudWord("westernunion"))));

        Verdict.VerdictType verdict = sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);

        assertThat(verdict, is(Verdict.VerdictType.BAD));
    }

    @Test
    public void doesNotAcceptAdWithUserHavingCriminalRecord() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.EVIL));

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        Verdict.VerdictType verdict = sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);

        assertThat(verdict, is(Verdict.VerdictType.BAD));
    }

    @Test
    public void repliesWithPendingVerdictWhenVettingTimeoutReached() {
        TestActorRef<VettingSubActor> vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS));

        sender.send(vettingActor, createAd());

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, new CheckUserResult(UserCriminalRecord.GOOD));
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, new ExamineWordsResult(List.empty()));

        assertThat(sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class), is(Verdict.VerdictType.PENDING));
    }

    @Test
    public void repliesWithPendingVerdictWhenUserActorTerminates() {
        FiniteDuration avoidTriggerTimeout = Duration.create(1, TimeUnit.DAYS);

        TestActorRef<VettingSubActor> vettingActor = createVettingActor(avoidTriggerTimeout);

        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());
        userActor.send(userActor.ref(), PoisonPill.getInstance());

        assertThat(sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class), is(Verdict.VerdictType.PENDING));
    }

    @Test
    public void terminatesAfterAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        TestActorRef<VettingSubActor> vettingActor = createVettingActor();
        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated.class);
    }

    @Test
    public void terminatesAfterNotAcceptAdWithFraudWords() {
        TestActorRef<VettingSubActor> vettingActor = createVettingActor();
        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.of(new FraudWord("westernunion"))));

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated.class);
    }

    @Test
    public void terminatesAfterVettingTimeoutReached() {
        TestActorRef<VettingSubActor> vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS));
        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, new CheckUserResult(UserCriminalRecord.GOOD));
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, new ExamineWordsResult(List.empty()));

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated.class);
    }

    @Test
    public void terminatesAfterUserActorTerminates() {
        FiniteDuration avoidTriggerTimeout = Duration.create(1, TimeUnit.DAYS);
        TestActorRef<VettingSubActor> vettingActor = createVettingActor(avoidTriggerTimeout);

        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());

        userActor.send(userActor.ref(), PoisonPill.getInstance());

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Verdict.VerdictType.class);
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated.class);
    }

    private Ad createAd() {
        return new Ad(1, "Sofa", "Selling sofa");
    }

    private TestActorRef<VettingSubActor> createVettingActor() {
        return createVettingActor(Duration.create(10, TimeUnit.SECONDS));
    }

    private TestActorRef<VettingSubActor> createVettingActor(FiniteDuration timeoutVetting) {
        return TestActorRef.create(system, Props.create(VettingSubActor.class, () -> new VettingSubActor(userActor.ref(), fraudWordActor.ref(), timeoutVetting)));
    }
}