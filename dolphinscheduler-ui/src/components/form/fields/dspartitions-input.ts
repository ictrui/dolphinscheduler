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

import { defineComponent, h, renderSlot } from 'vue'
import { useFormItem } from 'naive-ui/es/_mixins'
import { NFormItemGi, NSpace, NGrid, NInput } from 'naive-ui'
import { isFunction } from 'lodash'
import type { IJsonItem, FormItemRule } from '../types'
import { useI18n } from 'vue-i18n'

const DspartitionInput = defineComponent({
  name: 'MultiInput',
  setup() {
    const formItem = useFormItem({})

    return { disabled: formItem.mergedDisabledRef }
  },
  render() {
    const { disabled, $slots } = this

    return h(
      NSpace,
      { vertical: true, style: { width: '100%' } },
      {
        default: () => {
          return [renderSlot($slots, 'default', { disabled })]
        }
      }
    )
  }
})

export function renderDspartitionInput(
  item: IJsonItem,
  fields: { [field: string]: any },
  unused: { [key: string]: FormItemRule }[]
) {
  const { t } = useI18n()
  const { field, props } = isFunction(item) ? item() : item

  const getChild = (value: string, i: number) => {
    const [prefix, v] = value.split('=')
    return [
      h(
        NFormItemGi,
        {
          showLabel: true,
          path: `${field}[${i}]`,
          span: 24,
          label: prefix + '=',
          labelPlacement: 'left',
          labelStyle: 'min-width:100px;'
        },
        () =>
          h(NInput, {
            ...props,
            value: v,
            onUpdateValue: (value: string) =>
              void (fields[field][i] = prefix + '=' + value)
          })
      )
    ]
  }

  //initialize the component by using data
  const getChildren = () => {
    if (!fields[field] || fields[field].length === 0) {
      return [
        h(NGrid, { xGap: 10 }, () => [
          h(
            NFormItemGi,
            {
              showLabel: false,
              span: 24
            },
            () =>
              h(NInput, {
                ...props,
                value: t('project.node.hive_text')
              })
          )
        ])
      ]
    }
    return fields[field].map((value: string, i: number) => {
      return h(NGrid, { xGap: 10 }, () => [...getChild(value, i)])
    })
  }

  const { showTips } = props
  const tips = h(
    NFormItemGi,
    {
      showLabel: false,
      span: 24
    },
    () => h('div', { style: 'color:#ff0000' }, t('project.node.hive_tips'))
  )

  const renderDefault = [...getChildren()]
  showTips && renderDefault.push(tips)

  return h(
    DspartitionInput,
    {
      name: field
    },
    {
      default: () => renderDefault
    }
  )
}
