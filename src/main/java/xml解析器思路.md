- buildDocument方法思路：
  - 获取pojo所有字段的说明
    - 字段是否是简单类型：
      - 创建一个节点
      - 挂载到parent
    - 字段是否是pojo：
      - 根据该pojo创建一个根节点
      - 将根节点挂载到parent
      - 递归调用buildDocument
    - 字段是否集合：
      - 遍历集合：
        - 集合数据是简单类型：
          - 创建一个节点
          - 挂载到parent
        - 集合是pojo类型：
          - 根据该pojo创建一个根节点
          - 将根节点挂载到parent
          - 递归调用buildDocument
    - 字段是属性：
      - 字段是否是parent的属性：
        - 是：
          - 为parent添加一个属性
        - 否：
          - 从parent中搜索是否包含属性对应的节点：
            - 不包含：
               - 创建一个节点
               - 挂载到parent
          - 为节点添加属性