package com.blog.base.region;

import com.blog.base.it.IntegrationTest;
import com.blog.base.region.dto.RegionTreeNode;
import com.blog.base.region.dto.RegionVO;
import com.blog.base.region.service.RegionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executes the generated production seed script (02_region_data.sql) against
 * H2 and verifies its contents and the tree service. This doubles as a SQL
 * syntax/smoke check for the generated 3429-row seed file.
 */
@IntegrationTest
class RegionSeedIT {

    /** rows produced by scripts/gen-region-sql.mjs from modood pca-code.json */
    private static final int EXPECTED_TOTAL = 3429;
    private static final int EXPECTED_PROVINCES = 31;

    @Autowired
    private RegionService regionService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void productionSeedScript_shouldLoadAndBeQueryable() throws Exception {
        loadSeedIfEmpty();

        assertThat(regionService.count()).isEqualTo(EXPECTED_TOTAL);
        assertThat(regionService.listProvinces()).hasSize(EXPECTED_PROVINCES);

        RegionVO dongcheng = regionService.getByCode("110101");
        assertThat(dongcheng.getName()).isEqualTo("东城区");

        List<RegionTreeNode> tree = regionService.fullTree();
        assertThat(tree).hasSize(EXPECTED_PROVINCES);
        RegionTreeNode beijing = tree.stream()
                .filter(p -> "北京市".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(beijing.getChildren()).isNotEmpty();
        assertThat(beijing.getChildren().get(0).getChildren())
                .as("cities should contain districts")
                .isNotEmpty();
    }

    @Test
    void subtree_shouldReturnDirectChildrenOnly() throws Exception {
        loadSeedIfEmpty();

        // Beijing city 市辖区 (id=2) -> districts include 东城区
        List<RegionTreeNode> districts = regionService.subtree(2L);
        assertThat(districts).extracting(RegionTreeNode::getCode).contains("110101");
        assertThat(districts).allSatisfy(d -> assertThat(d.getLevel()).isEqualTo(3));
    }

    private void loadSeedIfEmpty() throws Exception {
        // NB: use JdbcTemplate here, not regionService.count(): inside the
        // test transaction MyBatis reuses one SqlSession and its first-level
        // cache would cache the pre-insert count(0) for the same statement.
        Integer existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM base_region", Integer.class);
        if (existing != null && existing > 0) {
            return;
        }
        String sql = new String(new ClassPathResource("db/init/02_region_data.sql").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        // H2 has no USE statement; the generated script targets MySQL's blog_base schema
        sql = sql.lines()
                .filter(line -> !line.stripLeading().startsWith("USE "))
                .reduce((a, b) -> a + System.lineSeparator() + b)
                .orElseThrow();

        ScriptUtils.executeSqlScript(DataSourceUtils.getConnection(dataSource),
                new EncodedResource(
                        new org.springframework.core.io.ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8));
    }
}
