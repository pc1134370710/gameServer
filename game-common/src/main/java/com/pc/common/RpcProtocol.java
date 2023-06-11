/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pc.common;


import com.alibaba.fastjson.JSON;
import com.pc.common.msg.Msg;

/**
 * Netty 通信的数据格式
 */
public class RpcProtocol {

    /**
     * 发送的数据在管道里是无缝流动的，在数据量很大时，为了分割数据，采用以下几种方法
     * 定长方法
     * 固定分隔符
     * 将消息分成消息体和消息头，在消息头中用一个数组说明消息体的长度
     */
    /**
     * 数据大小
     */
    private int len;

    /**
     * 数据内容
     */
    private byte[] content;

    public RpcProtocol(byte[] content) {
        this.content = content;
        this.len = content.length;
    }

    public RpcProtocol() {

    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
    public static RpcProtocol getRpcProtocol(Msg msg){
        String json = JSON.toJSONString(msg);
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setLen(json.getBytes().length);
        rpcProtocol.setContent(json.getBytes());
        return rpcProtocol;
    }
    @Override
    public String toString() {
        return "RpcProtocol{" +
                "len=" + len +
                ", content=" + new String(content) +
                '}';
    }
}
