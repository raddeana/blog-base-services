package com.blog.base.tag.controller;

import com.blog.base.common.exception.BizException;
import com.blog.base.common.exception.GlobalExceptionHandler;
import com.blog.base.tag.dto.TagVO;
import com.blog.base.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc test for {@link TagController}: request validation, the
 * Result envelope and BizException translation. Standalone setup (no Spring
 * context) avoids bootstrapping MyBatis mappers via the main class @MapperScan.
 */
@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    @BeforeEach
    void setUp() {
        // standalone setup auto-registers a JSR-303 validator when hibernate-validator is present
        mockMvc = MockMvcBuilders.standaloneSetup(tagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void create_blankName_shouldReturnParamErrorCode() throws Exception {
        mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"sort\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void listAll_shouldReturnResultEnvelope() throws Exception {
        TagVO vo = new TagVO();
        vo.setId(1L);
        vo.setName("Java");
        vo.setStatus(1);
        when(tagService.listEnabled()).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/v1/tags/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("Java"));
    }

    @Test
    void detail_serviceThrowsBiz_shouldTranslateToResult() throws Exception {
        when(tagService.detail(404L)).thenThrow(BizException.notFound());

        mockMvc.perform(get("/api/v1/tags/404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void create_validBody_shouldPassValidationAndReachService() throws Exception {
        TagVO vo = new TagVO();
        vo.setId(9L);
        vo.setName("Spring");
        when(tagService.create(any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring\",\"sort\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(9));
    }
}
