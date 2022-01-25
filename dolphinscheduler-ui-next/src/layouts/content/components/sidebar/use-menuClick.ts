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

import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { MenuOption } from 'naive-ui'
import { useMenuStore } from '@/store/menu/menu'

export function useMenuClick() {
  const router: Router = useRouter()
  const menuStore = useMenuStore()

  const handleMenuClick = (key: string, item: MenuOption) => {
    console.log(key, item)
    menuStore.setSideMenuKey(`${key}`)
    router.push({ path: `${key}` })
  }

  return {
    handleMenuClick
  }
}