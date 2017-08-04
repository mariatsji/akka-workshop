package workshop.part2;

import java.util.concurrent.TimeUnit;

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
import workshop.part1.VerdictStatus;
import workshop.part2.FraudWordActor.ExamineWordsResult;
import workshop.part2.UserActor.CheckUser;
import workshop.part2.UserActor.CheckUserResult;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VettingActorTest extends AkkaTest {

    TestProbe userActor = TestProbe.apply(system);
    TestProbe fraudWordActor = TestProbe.apply(system);

    @Test
    public void acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict.verdictStatus, is(VerdictStatus.GOOD));
    }

    @Test
    public void doesNotAcceptAdWithFraudWords() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.GOOD));

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.of(new FraudWord("westernunion"))));

        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict.verdictStatus, is(VerdictStatus.BAD));
    }

    @Test
    public void doesNotAcceptAdWithUserHavingCriminalRecord() {
        sender.send(createVettingActor(), createAd());

        userActor.expectMsgClass(CheckUser.class);
        userActor.reply(new CheckUserResult(UserCriminalRecord.EVIL));

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords.class);
        fraudWordActor.reply(new ExamineWordsResult(List.empty()));

        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict.verdictStatus, is(VerdictStatus.BAD));
    }

    @Test
    public void terminatesAndDoesNotReplyWhenVettingTimeoutReached() {
        TestActorRef<VettingActor> vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS));

        sender.watch(vettingActor);
        sender.send(vettingActor, createAd());

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, new CheckUserResult(UserCriminalRecord.GOOD));
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, new ExamineWordsResult(List.empty()));

        sender.expectMsgClass(Terminated.class);
    }

    private Ad createAd() {
        return new Ad(1, "Sofa", "Selling sofa");
    }

    private TestActorRef<VettingActor> createVettingActor() {
        return createVettingActor(Duration.create(10, TimeUnit.SECONDS));
    }

    private TestActorRef<VettingActor> createVettingActor(FiniteDuration timeoutVetting) {
        return TestActorRef.create(system, Props.create(VettingActor.class, () -> new VettingActor(userActor.ref(), fraudWordActor.ref(), timeoutVetting)));
    }
}