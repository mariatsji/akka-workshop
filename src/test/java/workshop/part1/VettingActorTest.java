package workshop.part1;

import akka.actor.Props;
import akka.testkit.TestActorRef;
import javaslang.collection.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import workshop.ad.ClassifiedAd;
import workshop.fraudwordsservice.FraudWordService;
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

        ClassifiedAd ad = new ClassifiedAd(1, "Sofa", "Selling sofa");
        createVettingActor().tell(ad, sender.ref());
        Verdict verdict = sender.expectMsgClass(Verdict.class);

        assertThat(verdict.verdictStatus, is(VerdictStatus.GOOD));
    }

    private TestActorRef<VettingActor> createVettingActor() {
        return TestActorRef.create(system, Props.create(VettingActor.class, () -> new VettingActor(userService, fraudWordService)));
    }
}