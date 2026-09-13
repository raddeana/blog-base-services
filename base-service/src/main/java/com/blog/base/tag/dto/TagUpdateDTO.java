package com.blog.base.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新标签")
public class TagUpdateDTO {

    @NotBlank(message = "标签名不能为空")
    @Size(max = 32, message = "标签名最长32字符")
    @Schema(description = "标签名", example = "Java")
    private String name;

    @Schema(description = "排序，越小越前", example = "0")
    private Integer sort;

    @Size(max = 255, message = "备注最长255字符")
    @Schema(description = "备注", example = "后端语言")
    private String remark;
}
