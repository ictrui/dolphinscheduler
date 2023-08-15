/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_plugin_define")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginDefine {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * plugin name
     */
    @TableField("plugin_name")
    private String pluginName;

    /**
     * plugin_type
     */
    @TableField("plugin_type")
    private String pluginType;

    /**
     * plugin_params
     */
    @TableField("plugin_params")
    private String pluginParams;

    /**
     * plugin_location
     */
    @TableField("plugin_location")
    private String pluginLocation;

    /**
     * plugin full class name
     */
    @TableField("plugin_class_name")
    private String pluginClassName;

    /**
     * create_time
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * update_time
     */
    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private String originalFileName;

    public PluginDefine(String pluginName, String pluginType, String pluginParams) {
        this.pluginName = pluginName;
        this.pluginType = pluginType;
        this.pluginParams = pluginParams;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public PluginDefine(String pluginName, String pluginType, String pluginParams, String pluginLocation,
                        String pluginClassName) {
        this.pluginName = pluginName;
        this.pluginType = pluginType;
        this.pluginParams = pluginParams;
        this.pluginLocation = pluginLocation;
        this.pluginClassName = pluginClassName;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public void setPluginLocation(String pluginLocation) {
        this.pluginLocation = pluginLocation;
        if (Objects.nonNull(pluginLocation)) {
            String[] split = pluginLocation.split("/");
            this.originalFileName = split[split.length - 1].split("@")[0] + ".jar";
        }
    }
}
