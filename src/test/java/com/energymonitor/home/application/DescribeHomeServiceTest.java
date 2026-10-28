package com.energymonitor.home.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.energymonitor.home.application.port.in.DescribeHome.HomeSummary;
import com.energymonitor.home.application.usecase.DescribeHomeService;
import com.energymonitor.home.domain.model.Home;
import com.energymonitor.home.domain.model.UserHome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Home and members asked by another service")
class DescribeHomeServiceTest {

    private static final String HOME = "hom0000001";
    private static final String OWNER = "use0000001";
    private static final String MEMBER = "use0000002";

    private HomeUseCaseFixtures.FakeHomePersistencePort homes;
    private HomeUseCaseFixtures.FakeUserHomePersistencePort memberships;
    private DescribeHomeService service;

    @BeforeEach
    void setUp() {
        homes = new HomeUseCaseFixtures.FakeHomePersistencePort();
        memberships = new HomeUseCaseFixtures.FakeUserHomePersistencePort();
        service = new DescribeHomeService(homes, memberships);
        homes.save(Home.create(HOME, "Casa", "hous000001", "Calle 1", "AAA11111", null,
                HomeUseCaseFixtures.NOW));
        memberships.save(UserHome.owner(OWNER, HOME));
        memberships.save(UserHome.member(MEMBER, HOME));
    }

    @Test
    @DisplayName("the home with every member, owners included")
    void homeWithMembers() {
        HomeSummary summary = service.describe(HOME).orElseThrow();

        assertThat(summary.homeId()).isEqualTo(HOME);
        assertThat(summary.name()).isEqualTo("Casa");
        assertThat(summary.homeTypeId()).isEqualTo("hous000001");
        assertThat(summary.address()).isEqualTo("Calle 1");
        assertThat(summary.creationDate()).isEqualTo(HomeUseCaseFixtures.NOW);
        assertThat(summary.memberIds()).containsExactlyInAnyOrder(OWNER, MEMBER);
    }

    @Test
    @DisplayName("a member who left is not listed")
    void formerMember() {
        memberships.remove(MEMBER, HOME);

        assertThat(service.describe(HOME).orElseThrow().memberIds()).containsExactly(OWNER);
    }

    @Test
    @DisplayName("nothing for a missing home")
    void missingHome() {
        assertThat(service.describe("hom9999999")).isEmpty();
    }
}
