package com.wcy.rhapsody.admin.model.vo;

import lombok.Data;

/**
 * Vditor文件上传返回
 *
 * @author Yeeep
 * @date 2020/11/29
 */
@Data
public class FileVO {

    // msg: '',
    // code: 0,
    // data : {
    //     originalURL: '',
    //             url: ''
    // }

    private Integer code;

    private String msg;

    private Object data;

}