/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xutils.http.annotation;

import org.xutils.http.app.DefaultParamsBuilder;
import org.xutils.http.app.ParamsBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpRequest {

//    String host() default "http://zyp.szprize.cn/appstore";
//	String host() default "http://192.168.1.158:8083";
	String host() default "http://launcher.szprize.cn";

    String path();

    Class<? extends ParamsBuilder> builder() default DefaultParamsBuilder.class;

    String[] signs() default "";

    String[] cacheKeys() default "";
}