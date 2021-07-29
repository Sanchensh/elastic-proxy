/*
 * Copyright (c) 2014 AsyncHttpClient Project. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 *     http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.mrxu.netty.client;

import com.mrxu.netty.SessionContext;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import static com.mrxu.common.Constants.SESSION_ATTRIBUTE;

/**
 * Channel工具类
 */
public class ChannelUtil {

    public static final AttributeKey<SessionContext> attributeKey = AttributeKey.valueOf(SESSION_ATTRIBUTE);

    //获取SessionContext
    public static SessionContext getSessionContext(Channel channel) {
        return channel.attr(attributeKey).get();
    }

    //设置ChannelAttribute属性
    public static void attributeSessionContext(Channel channel, SessionContext sessionContext) {
        Attribute<SessionContext> attr = channel.attr(attributeKey);
        attr.set(sessionContext);
    }

    //清除SessionContext
    public static void clearSessionContext(Channel channel) {
        channel.attr(attributeKey).set(null);
    }
}
