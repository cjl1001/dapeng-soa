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
package com.github.dapeng.router;

/**
 * 路由匹配成功后，导向的具体目标
 *
 * @author huyj
 * @Created 2019-01-17 11:17
 */
public class CommonThen {
    public final boolean not;
    private final int routeType;

    public CommonThen(int routeType, boolean not) {
        this.not = not;
        this.routeType = routeType;
    }

    public boolean isNot() {
        return not;
    }

    public int getRouteType() {
        return routeType;
    }

    @Override
    public String toString() {
        return "CommonThen{" +
                "not=" + not +
                ", routeType=" + routeType +
                '}';
    }
}