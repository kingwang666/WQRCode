package com.wang.qrcode.model;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/16
 */

public class ScanResult {

    public boolean success;

    public ParsedResult result;

    public ScanResult(boolean success, ParsedResult result) {
        this.success = success;
        this.result = result;
    }
}
