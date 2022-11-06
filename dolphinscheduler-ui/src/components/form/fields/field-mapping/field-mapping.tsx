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

import { computed, defineComponent, h, watch } from 'vue'
import { isFunction } from 'lodash'
import type { IJsonItem, fieldMappingListItem } from '../../types'
import { useFormItem } from 'naive-ui/es/_mixins'
import {
  DataTableColumns,
  NButton,
  NCheckbox,
  NDataTable,
  NInput,
  useMessage
} from 'naive-ui'
import './style.scss'

const FieldMapping = defineComponent({
  name: 'FieldMapping',
  props: {
    field1: {
      default: '',
      type: String
    },
    field2: {
      default: [],
      type: String
    },
    fields: {
      default: {},
      type: Object
    }
  },
  setup({ field1, field2, fields }) {
    const message = useMessage()
    const formItem = useFormItem({})
    const disabled = computed(() => {
      return (
        formItem.mergedDisabledRef.value ||
        !fields['sourceTable'] ||
        !(fields['dtType'] === 'ELASTICSEARCH'
          ? fields['dataTarget']
          : fields['targetTable'])
      )
    })

    const exchange = (f: string, set: 'top' | 'bottom', index: number) => {
      if (set === 'top') {
        // eslint-disable-next-line vue/no-mutating-props
        fields[f][index]['enable'] = false
        // eslint-disable-next-line vue/no-mutating-props
        fields[f][index - 1]['enable'] = false
        ;[fields[f][index], fields[f][index - 1]] = [
          fields[f][index - 1],
          fields[f][index]
        ]
      }
      if (set === 'bottom') {
        // eslint-disable-next-line vue/no-mutating-props
        fields[f][index]['enable'] = false
        // eslint-disable-next-line vue/no-mutating-props
        fields[f][index + 1]['enable'] = false
        ;[fields[f][index], fields[f][index + 1]] = [
          fields[f][index + 1],
          fields[f][index]
        ]
      }
    }

    const connect = (is: boolean, index: number) => {
      // eslint-disable-next-line vue/no-mutating-props
      fields[field1][index]['enable'] = is
      // eslint-disable-next-line vue/no-mutating-props
      fields[field2][index]['enable'] = is
    }

    const connectAll = (is: boolean) => {
      if (fields[field1].length && fields[field2].length) {
        centerData.value.forEach((s, index) => {
          // eslint-disable-next-line vue/no-mutating-props
          fields[field1][index]['enable'] = is
          // eslint-disable-next-line vue/no-mutating-props
          fields[field2][index]['enable'] = is
        })
      }
    }

    const emptyColItem = (): fieldMappingListItem => ({
      index: 0,
      columnName: '',
      dataType: 'custom',
      enable: false,
      json: ''
    })

    const addCol = (f: string) => {
      // eslint-disable-next-line vue/no-mutating-props
      fields[f].push(emptyColItem())
    }

    const delCol = (f: string, index: number) => {
      // eslint-disable-next-line vue/no-mutating-props
      fields[field1][index] && (fields[field1][index]['enable'] = false)
      // eslint-disable-next-line vue/no-mutating-props
      fields[field2][index] && (fields[field2][index]['enable'] = false)
      // eslint-disable-next-line vue/no-mutating-props
      fields[f].splice(index, 1)
    }

    const columns1: DataTableColumns = [
      {
        title() {
          return (
            <div>
              源头表字段
              <NButton
                disabled={disabled.value}
                onClick={() => {
                  addCol(field1)
                }}
                quaternary
                type='info'
                size='tiny'
              >
                +
              </NButton>
            </div>
          )
        },
        key: 'columnName',
        align: 'center',
        width: 120,
        render(row) {
          return (
            <NInput
              disabled={disabled.value}
              value={String(row.columnName)}
              onUpdateValue={(v) => {
                row.columnName = v
              }}
            />
          )
        }
      },
      {
        title: '类型',
        key: 'dataType',
        align: 'center',
        ellipsis: true
      },
      {
        title: '操作',
        key: 'action',
        width: 120,
        align: 'center',
        render(row, index) {
          const length = fields[field1].length
          return (
            <div>
              {index !== 0 && (
                <NButton
                  disabled={disabled.value}
                  onClick={() => exchange(field1, 'top', index)}
                  tertiary
                  circle
                  size='small'
                >
                  ↑
                </NButton>
              )}
              {index < length - 1 && (
                <NButton
                  disabled={disabled.value}
                  onClick={() => exchange(field1, 'bottom', index)}
                  tertiary
                  circle
                  size='small'
                >
                  ↓
                </NButton>
              )}
              <NButton
                disabled={disabled.value}
                onClick={() => delCol(field1, index)}
                tertiary
                circle
                size='small'
              >
                ✖
              </NButton>
            </div>
          )
        }
      }
    ]
    const columns2: DataTableColumns = [
      {
        title: '目标表字段',
        key: 'columnName',
        align: 'center',
        width: 120,
        render(row) {
          return (
            <NInput
              disabled={disabled.value}
              value={String(row.columnName)}
              onUpdateValue={(v) => {
                row.columnName = v
              }}
            />
          )
        }
      },
      {
        title: '类型',
        key: 'dataType',
        align: 'center',
        ellipsis: true
      },
      {
        title: '操作',
        key: 'action',
        align: 'center',
        width: 120,
        render(row, index) {
          const length = fields[field2].length
          return (
            <div>
              {index !== 0 && (
                <NButton
                  disabled={disabled.value}
                  onClick={() => exchange(field2, 'top', index)}
                  tertiary
                  circle
                  size='small'
                >
                  ↑
                </NButton>
              )}
              {index < length - 1 && (
                <NButton
                  disabled={disabled.value}
                  onClick={() => exchange(field2, 'bottom', index)}
                  tertiary
                  circle
                  size='small'
                >
                  ↓
                </NButton>
              )}
              <NButton
                disabled={disabled.value}
                onClick={() => delCol(field2, index)}
                tertiary
                circle
                size='small'
              >
                ✖
              </NButton>
            </div>
          )
        }
      }
    ]
    const columns2ForEs: DataTableColumns = [
      {
        title() {
          return (
            <div>
              目标表字段
              <NButton
                disabled={disabled.value}
                onClick={() => {
                  addCol(field2)
                }}
                quaternary
                type='info'
                size='tiny'
              >
                +
              </NButton>
            </div>
          )
        },
        key: 'json',
        align: 'center',
        width: 200,
        render(row: { [key: string]: any }) {
          return (
            <NInput
              disabled={disabled.value}
              value={String(row.json)}
              placeholder='JSON'
              onBlur={() => {
                let s = ''
                try {
                  s = JSON.parse(row.json)
                  row.json = JSON.stringify(s, null, 2)
                } catch (e) {
                  message.warning('错误的JSON格式')
                  row.json = ''
                }
              }}
              onUpdateValue={(v) => {
                row.json = v
              }}
            />
          )
        }
      },
      {
        title: '操作',
        key: 'action',
        align: 'center',
        width: 120,
        render(row, index) {
          const length = fields[field2].length
          return (
            <div>
              {index !== 0 && (
                <NButton
                  disabled={disabled.value}
                  onClick={() => exchange(field2, 'top', index)}
                  tertiary
                  circle
                  size='small'
                >
                  ↑
                </NButton>
              )}
              {index < length - 1 && (
                <NButton
                  disabled={disabled.value}
                  onClick={() => exchange(field2, 'bottom', index)}
                  tertiary
                  circle
                  size='small'
                >
                  ↓
                </NButton>
              )}
              <NButton
                disabled={disabled.value}
                onClick={() => delCol(field2, index)}
                tertiary
                circle
                size='small'
              >
                ✖
              </NButton>
            </div>
          )
        }
      }
    ]
    const columnsCenter: DataTableColumns = [
      {
        title() {
          return (
            <div>
              连接
              <NCheckbox
                style='marginLeft:2px'
                disabled={disabled.value}
                checked={
                  centerData.value.length &&
                  centerData.value.every((s) => s.isCheck)
                }
                onUpdateChecked={(is: boolean) => {
                  connectAll(is)
                }}
              />
            </div>
          )
        },
        key: 'action',
        align: 'center',
        render(row, index: number) {
          return (
            <div class='content'>
              <NCheckbox
                disabled={disabled.value}
                checked={!!row.isCheck}
                onUpdateChecked={(is: boolean) => {
                  connect(is, index)
                }}
              />
            </div>
          )
        }
      }
    ]

    const centerData = computed((): { isCheck: boolean }[] => {
      const minLength = Math.min(fields[field1].length, fields[field2].length)
      return minLength
        ? new Array(minLength).fill({}).map((row, index) => ({
            isCheck:
              fields[field1][index]['enable'] && fields[field2][index]['enable']
          }))
        : []
    })

    watch([() => fields[field1], () => fields[field2]], () => {
      if (fields[field2].length && !disabled.value) {
        const length = fields[field2].length - fields[field1].length
        if (length > 0)
          // eslint-disable-next-line vue/no-mutating-props
          fields[field1].push(
            ...new Array(length)
              .fill(null)
              .map((): fieldMappingListItem => emptyColItem())
          )
      }

      if (
        fields[field1].length &&
        fields['dtType'] === 'ELASTICSEARCH' &&
        !disabled.value
      ) {
        const length = fields[field1].length - fields[field2].length
        if (length > 0)
          // eslint-disable-next-line vue/no-mutating-props
          fields[field2].push(
            ...new Array(length)
              .fill(null)
              .map((): fieldMappingListItem => emptyColItem())
          )
      }
    })

    return {
      disabled,
      columns1,
      columns2,
      centerData,
      columnsCenter,
      columns2ForEs
    }
  },
  render() {
    return (
      <div class='box'>
        <div class='left'>
          <NDataTable
            columns={this.columns1}
            data={this.fields[this.field1]}
            pagination={false}
            bordered={false}
          ></NDataTable>
        </div>
        <div class='center'>
          <NDataTable
            columns={this.columnsCenter}
            data={this.centerData}
            pagination={false}
            bordered={false}
          />
        </div>
        <div class='right'>
          <NDataTable
            columns={
              this.fields['dtType'] === 'ELASTICSEARCH'
                ? this.columns2ForEs
                : this.columns2
            }
            data={this.fields[this.field2]}
            pagination={false}
            bordered={false}
          />
        </div>
      </div>
    )
  }
})

export function renderFieldMapping(
  item: IJsonItem,
  fields: { [field: string]: any }
) {
  const { props, field } = isFunction(item) ? item() : item
  const [field1, field2] = field.split(':')
  return h(FieldMapping, {
    ...props,
    field1,
    field2,
    fields
  })
}
