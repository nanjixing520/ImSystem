package com.lld.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * ClassName: ImportUserResp
 * Package: com.lld.im.service.user.model.resp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午5:20
 * Version 1.0
 */
@Data
public class ImportUserResp {

    private List<String> successId;

    private List<String> errorId;

}
