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
import * as Field from './index'
import { camelCase, upperFirst, isFunction } from 'lodash'
import type { FormRules, FormItemRule } from 'naive-ui'
import type { IJsonItem } from '../types'
import { IType } from '../types'

const TYPES: IType[] = [
  'input',
  'radio',
  'editor',
  'custom-parameters',
  'switch',
  'input-number',
  'select',
  'filterable-select',
  'checkbox',
  'tree-select',
  'multi-input',
  'custom',
  'multi-condition',
  'dspartition-input',
  'divider',
  'field-mapping'
]

const getField = (
  item: IJsonItem,
  fields: { [field: string]: any },
  rules?: FormRules
) => {
  const { type = 'input', widget, field } = isFunction(item) ? item() : item
  if (!TYPES.includes(type)) return null
  const renderTypeName = `render${upperFirst(camelCase(type))}`
  if (type === 'custom') {
    return widget || null
  }

  if (!field) {
    // @ts-ignore
    return Field[renderTypeName](item, fields)
  }
  //支持表单嵌套层级 例如form.data.name
  const params = {
    item,
    fields
  }
  const fieldArr = field.split('.')
  for (let i = 0; i < fieldArr.length - 1; i++) {
    params.item = { ...item, field: fieldArr[i + 1] }
    params.fields = params.fields[fieldArr[i]]
  }

  // TODO Support other widgets later
  if (type === 'custom-parameters') {
    let fieldRules: { [key: string]: FormItemRule }[] = []
    if (rules && !rules[field]) fieldRules = rules[field] = []
    // @ts-ignore
    return Field[renderTypeName](params.item, params.fields, fieldRules)
  }
  // @ts-ignore
  return Field[renderTypeName](params.item, params.fields)
}

export default getField
