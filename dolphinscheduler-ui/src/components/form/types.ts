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
import { Ref } from 'vue'
import type { GridProps, FormProps, FormItemRule, FormRules } from 'naive-ui'

type IType =
  | 'input'
  | 'radio'
  | 'editor'
  | 'custom-parameters'
  | 'switch'
  | 'input-number'
  | 'select'
  | 'checkbox'
  | 'tree-select'
  | 'multi-input'
  | 'custom'
  | 'multi-condition'
  | 'dspartition-input'
  | 'divider'
  | 'field-mapping'

interface IOption {
  [key: string]: any
}

interface IFormItem {
  showLabel?: boolean
  path: string
  label?: string
  widget: any
  span?: number | Ref<number>
  type?: 'custom'
  class?: string
}

interface IMeta extends Omit<FormProps, 'model'> {
  elements?: IFormItem[]
  model: object
}

interface IJsonItemParams {
  field: string
  name?: string
  props?: any
  title?: string
  type?: IType
  validate?: FormItemRule
  value?: any
  options?: IOption[] | Ref<IOption[]>
  children?: IJsonItem[]
  slots?: object
  span?: number | Ref<number>
  widget?: any
  class?: string
  path?: string
  rule?: FormItemRule
  group?: string
}

type IJsonItemFn = (i?: number) => IJsonItemParams

type IJsonItem = IJsonItemParams | IJsonItemFn

interface elasticSearchDataxParams {
  index: string
  type?: string
  cleanUp?: boolean
  splitter?: string
  tyrSize?: number
  timeout?: number
  discovery?: boolean
  compression?: boolean
  multiThread?: boolean
  ignoreWriteError?: boolean
  ignoreParseError?: boolean
  alias?: string
  aliasMode?: number
  settings?: string
}

interface fieldMappingListItem {
  index: number
  columnName: string
  dataType: string
  enable: boolean
  json: string
}

export {
  IMeta,
  IType,
  IJsonItem,
  IOption,
  FormItemRule,
  FormRules,
  IFormItem,
  GridProps,
  IJsonItemParams,
  elasticSearchDataxParams,
  fieldMappingListItem
}
