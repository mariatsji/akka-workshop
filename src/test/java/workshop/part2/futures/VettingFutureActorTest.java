package workshop.part2.futures;

import java.util.concurrent.TimeUnit;

import akka.actor.Props;
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
import workshop.part2.subactor.VettingActor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VettingFutureActorTest extends AkkaTest {

    TestProbe userActor = TestProbe.apply(system);
    TestProbe fraudWordActor = TestProbe.apply(system);

    @Test
    public void acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        Verdict.VerdictType verdict = sender.expectMsgClass(Verdict.VerdictType.class);

        assertThat(verdict, is(Verdict.VerdictType.GOOD));
    }

    @Test
    public void doesNotAcceptAdWithFraudWords() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.of(new FraudWord("westernunion"))));

        Verdict.VerdictType verdict = sender.expectMsgClass(Verdict.VerdictType.class);

        assertThat(verdict, is(Verdict.VerdictType.BAD));
    }

    @Test
    public void doesNotAcceptAdWithUserHavingCriminalRecord() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.EVIL));

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        Verdict.VerdictType verdict = sender.expectMsgClass(Verdict.VerdictType.class);

        assertThat(verdict, is(Verdict.VerdictType.BAD));
    }

    @Test
    public void repliesWithUnknownVerdictWhenVettingTimeoutReached() {
        TestActorRef<VettingActor> vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS));

        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());

        schedule(Duration.create(500, TimeUnit.MILLISECONDS), vettingActor, new CheckUserResult(UserCriminalRecord.GOOD));
        schedule(Duration.create(500, TimeUnit.MILLISECONDS), vettingActor, new ExamineWordsResult(List.empty()));

        assertThat(sender.expectMsgClass(Verdict.VerdictType.class), is(Verdict.VerdictType.PENDING));
    }

    private Ad createAd() {
        return new Ad(1, "Sofa", "Selling sofa");
    }

    private TestActorRef<VettingActor> createVettingActor() {
        return createVettingActor(Duration.create(10, TimeUnit.SECONDS));
    }

    private TestActorRef<VettingActor> createVettingActor(FiniteDuration timeoutVetting) {
        return TestActorRef.create(system, Props.create(VettingFutureActor.class, () -> new VettingFutureActor(userActor.ref(), fraudWordActor.ref(), timeoutVetting)));
    }
}