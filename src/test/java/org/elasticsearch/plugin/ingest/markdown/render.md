# 数据与渲染（UI）分离<!-- omit in toc -->

在web前端开发中，数据与渲染（UI）分离、数据驱动渲染（UI）是一种设计思路、编程方法，可以让我们解耦代码，更易开发测试和维护，本文讲解数据与渲染（UI）分离和数据驱动渲染（UI）的思路以及实践方法（规范）。

- [什么是数据与渲染（UI）分离](#%E4%BB%80%E4%B9%88%E6%98%AF%E6%95%B0%E6%8D%AE%E4%B8%8E%E6%B8%B2%E6%9F%93ui%E5%88%86%E7%A6%BB)
- [为什么要数据与渲染（UI）分离](#%E4%B8%BA%E4%BB%80%E4%B9%88%E8%A6%81%E6%95%B0%E6%8D%AE%E4%B8%8E%E6%B8%B2%E6%9F%93ui%E5%88%86%E7%A6%BB)
- [什么时候要数据与渲染（UI）分离](#%E4%BB%80%E4%B9%88%E6%97%B6%E5%80%99%E8%A6%81%E6%95%B0%E6%8D%AE%E4%B8%8E%E6%B8%B2%E6%9F%93ui%E5%88%86%E7%A6%BB)
- [数据驱动渲染（UI）](#%E6%95%B0%E6%8D%AE%E9%A9%B1%E5%8A%A8%E6%B8%B2%E6%9F%93ui)
- [实践方法](#%E5%AE%9E%E8%B7%B5%E6%96%B9%E6%B3%95)
	- [修改“数据”->requestRender->render](#%E4%BF%AE%E6%94%B9%E6%95%B0%E6%8D%AE-requestrender-render)
	- [虚拟渲染（虚拟滚动）](#%E8%99%9A%E6%8B%9F%E6%B8%B2%E6%9F%93%E8%99%9A%E6%8B%9F%E6%BB%9A%E5%8A%A8)
	- [复用DOM](#%E5%A4%8D%E7%94%A8dom)
	- [命名规范 - Builder和Editor](#%E5%91%BD%E5%90%8D%E8%A7%84%E8%8C%83---builder%E5%92%8Ceditor)
	- [渲染、交互、数据分离测试](#%E6%B8%B2%E6%9F%93%E4%BA%A4%E4%BA%92%E6%95%B0%E6%8D%AE%E5%88%86%E7%A6%BB%E6%B5%8B%E8%AF%95)
	- [先比较数据后修改DOM](#%E5%85%88%E6%AF%94%E8%BE%83%E6%95%B0%E6%8D%AE%E5%90%8E%E4%BF%AE%E6%94%B9dom)
	- [IDataProvider模式](#idataprovider%E6%A8%A1%E5%BC%8F)
	- [ICommandProvider模式 - “命令”也是“数据”](#icommandprovider%E6%A8%A1%E5%BC%8F---%E5%91%BD%E4%BB%A4%E4%B9%9F%E6%98%AF%E6%95%B0%E6%8D%AE)
- [参考实例](#%E5%8F%82%E8%80%83%E5%AE%9E%E4%BE%8B)

## 什么是数据与渲染（UI）分离

在以`HTML5`为基础的web前端开发中，渲染和UI我们可以理解为对`DOM`元素的增删改操作，那“数据”什么？

这里所说的“数据”泛指那些`DOM`元素以外的信息，比如类的成员变量、js类、json等，“数据”是和`DOM`无关的，对数据的修改往往很高效。所谓“数据与渲染（UI）分离”是指将对数据的“操作（增删改）”封装到独立的对象，然后将对`DOM`的“操作（增删改）”封装到另一个独立的对象，让数据对象可以独立的使用和测试，让UI对象依赖数据对象去渲染。

## 为什么要数据与渲染（UI）分离

有这么几个好处：

1. **性能更好**，性能更好得益于能更好的做到：少折腾DOM（达到同样一个目的，越少操作dom往往越快）、异步渲染、虚拟渲染、复用DOM……
2. **更易开发**，解偶后对象职责更清晰，再参考[实践方法](#实践方法)中的规范进行开发，也更易维护和分工开发，同时更易测试……
3. **更易测试**，将测试的工作分开三种：渲染、交互、数据，各自用不同的方法测试，见[渲染、交互、数据分离测试](#渲染、交互、数据分离测试)

## 什么时候要数据与渲染（UI）分离

下列这些情况需要考虑数据与渲染（UI）分离：

1. 有很多“数据”且需要追求效率的时候，比如tree有很多节点、菜单有很多子菜单、表格有很多单元格……。
2. 比较复杂，有很多业务逻辑和UI，比如表单填报界面、数据加工界面。
3. 同一份数据可能有不同显示视图时，比如元数据有缩略图和列表显示模式。
4. ……

当然，你只有在做界面UI开发的时候才需考虑“数据与渲染（UI）分离”，但只要你做前端开发，不管是小到一个编辑框控件还是复杂到一个数据加工模块，通常都需要考虑数据与渲染（UI）分离。

## 数据驱动渲染（UI）

界面UI开发过程中，随时都会根据用户的操作行为（如点击、拖动、快捷键……）修改UI的外观效果，当“数据与渲染（UI）分离”时我要让UI响应用户的操作发生变化不是要同时改“数据”又要同时改“UI”，这不是更麻烦吗？

让UI响应用户的操作发生变化，最好的方式是“数据驱动渲染（UI）”，最终的效果会让开发过程更简单，思路是：“修改数据” -> “请求渲染”，具体的方法见[修改“数据”->requestRender->render](#修改“数据”->requestRender->render)。

## 实践方法

遵守这些实践方法往往可以让我们的代码更规范，更易在团队中维护，这些实践方法也是规范。

### 修改“数据”->requestRender->render

修改“数据”->requestRender->render，这是[数据驱动渲染（UI）](#数据驱动渲染（UI）)的实践方法。要点如下：

1. 有`public requestRender(): Promise<void>`函数，用于“请求渲染”，请求渲染并不会立即执行渲染，渲染是异步的，可能请求了100次渲染，真正的渲染动作只执行一次。
2. 有`private render(): void`函数，这是真正的渲染函数，在此函数创建、复用和修改DOM，通常类中修改dom的地方都集中在此函数内，其他地方往往只需要修改数据不需要直接操作dom。
3. 控件提供的增删改操作的接口函数，例如`addItem, deleteItem, moveItem, setProperty……`等，往往只修改“数据”，然后调用`requestRender`函数。

示例可以参考`tree.js`中的List控件的实现：

```ts
/**
 * 标记为需要重新绘制，在系统有资源时会真正的执行绘制。
 */
public requestRender(isScroll?: boolean): Promise<List> {
	let renderPromise = this.renderPromise;
	if (renderPromise) {
		return renderPromise;
	}
	return this.renderPromise = new Promise(resolve => {
		requestAnimationFrame(() => {
			this.renderPromise = null;
			if (this.domBase && (isScroll || this.domBase.offsetParent)) {
				/**
				 * 有可能在将要渲染时tree已经被dispose了，所以需要判断domBase是否存在。
				 *
				 * 有时候tree被创建了，append到了一个隐藏的面板上（比如notebook的一个page），此时如果insertItem，那么会调用
				 * requestRender函数，但是由于tree还没有offsetParent，所以渲染也无意义，还可能导致无法获得正确的itemHeight，
				 * 所以这里做了判断，如果没有offsetParent就不渲染，等下次tree的容器显示时自然会分发doResize事件，那时再渲染
				 */
				this.render(isScroll);
			}
			resolve(this);
		});
	});
}

/**
 * 根据数据渲染UI。
 *
 * @param isScroll 如果是虚拟滚动那么往往需要这个参数，表示是否为滚动之后调用，主要用于优化doScroll之后render以及updateMacScollBar性能。
 */
private render(isScroll: boolean): void {
	// 在这里创建、复用和修改DOM，通常类中修改dom的地方都集中在此函数内，其他地方往往不需要直接操作dom。
}
```

### 虚拟渲染（虚拟滚动）

虚拟渲染（虚拟滚动）往往用在有大量数据的控件中，比如List控件可能有100+万个节点，虚拟渲染（虚拟滚动）的目的就是尽量减少实际创建的DOM元素，不管“数据”有多少，用虚拟滚动的技术“欺骗”用户的眼睛，只渲染出用户眼睛看得到的内容即可，使用此方法可以达到很高的效率，以List和Tree为例：

1. 往List中添加100w条数据并显示出来只需1s
2. 往Tree中添加100w条多级数据并显示出来只需1.5s（互联网上还没找到这么快的Tree控件）
3. 用户的后续操作，滚动、展开等不受影响
4. 时间复杂度接近于 O(1)

实践方法要点：

1. 控件内往往需要有很多`Item`对象，如List对象中的`ListItem`对象，`Item`对象不需要是`Component`是普通的`class`即可。
2. `Item`对象需要有这几个方法（参考List对象中的`ListItem`对象）：
	1. `public createDom(accept?: boolean): HTMLElement` - 当没有dom可用时，创建新的dom。
	2. `public acceptDom(dom: HTMLElement): void` - 复用已有的dom。
	3. `public releaseDom(): HTMLElement` - 释放对dom的引用，被释放的dom就可以被其它item对象再次使用了。
3. 控件的`requestRender`和`render`函数（见[修改“数据”->requestRender->render](#修改“数据”->requestRender->render)中的说明）接受`isScroll?: boolean`参数，用于优化滚动性能。

### 复用DOM

浏览器中的`DOM`元素是很**昂贵**的，web前端开发优化性能的主要途径就是尽量少“折腾”DOM，奈何这个世界处处存在“矛盾”，在浏览器上要显示一个UI又必须用DOM（先不考虑canvas），所以如果已存在的DOM可以用，复用它吧。

在[虚拟渲染（虚拟滚动）](#虚拟渲染（虚拟滚动）)说了控件内部的`Item`对象间的DOM复用，那么控件之间甚至页面之间是否也可以复用呢，当然可以，考虑这几个案例：

1. 在系统的“数据”模块连续打开10个数据表对象，此时创建了多少个DOM？
2. 每个数据表编辑器上都能弹出右键菜单，这个菜单的DOM创建了几个？
3. 每个数据表的字段编辑列表中双击都可以编辑字段长度，这个字段长度的输入框DOM创建了几个？

复用DOM的实践要点包括：

1. 如果一个控件虽然可能出现在很多地方，但是一次只会显示一个，比如右键菜单、颜色浮动框、某些对话框、单元格内部的编辑框等，通过创建全局的控件来复用DOM。
2. 如果用户可能打开很多页面，但是页面是在标签页下面的，一次只能激活查看一个页面，比如（“数据”模块连续打开10个数据表对象），此时可以复用页面对象，标签页有很多个，但是标签页下面的页面控件只有1个或2个，此时页面控件需要实现`sys.ts`中的`IStateRestorable`接口（参考`viewer-code.ts`中的MetaFileViewer_code类或`dwtable.ts`中的`DwTableEditor`类）。
3. 控件内部的`Item`对象间的DOM复用，见[虚拟渲染（虚拟滚动）](#虚拟渲染（虚拟滚动）)。

### 命名规范 - Builder和Editor

当一个模块（注意不是一个简单的控件）用“数据与渲染（UI）分离”的开发思路时，会涉及到一系列的数据类和UI类，参考数据表编辑器`dwtable.ts`，那么最好遵守这个约定：

1. 所有Builder结尾的类都是“数据”类
2. 所有Editor结尾的类都是“渲染/UI”类

### 渲染、交互、数据分离测试

测试驱动开发的重要性无需多说，但是web前端开发涉及到很多UI，测试起来甚是麻烦，当“数据与渲染（UI）分离”时，测试工作也需要进行分离测试，往往可以达到很好的效果，我们通常把前端开发的测试分这么几类：

1. **数据（逻辑）测试** - 包含数据的各种增删改操作，包含几乎所有的业务逻辑。
2. **渲染（UI）测试** - 测试数据到UI的渲染逻辑过程，往往不包含业务逻辑。
3. **交互测试** - 测试界面上的操作是否正常，测试对事件的相应效果。

这几类测试的实践要点（可参考`fappflow.qunit.ts`）主要体现在测试函数的命名和断言比对逻辑上，测试函数必须符合下面的命名和断言规范，具体如下：

1. `export async function test_data_xxx(assert: Assert)` - 数据（逻辑）测试，这类用例往往会比较多，测试过程中只需要断言比对数据即可。
2. `export async function test_render_xxx(assert: Assert)` - 渲染（UI）测试，需要的用例数较少，测试过程必须断言比对DOM。
3. `export async function test_interact_xxx(assert: Assert)` - 交互测试，用例数最少，测试过程往往更多的需要断言比对DOM。

### 先比较数据后修改DOM

大的控件如List、Tree，它们可以“数据与渲染（UI）分离”开发，那一个按钮，一个输入框，这样的小控件呢，这样的小控件，往往就是一个`class`，在`class`内部进行“分离”，比如`Edit`控件，会在类的成员变量上记录各种数据，包括：value、visible、placeholder等，当用户调用`setXxxx()`函数时，我们需要**先比较数据后修改DOM**，以`Edit`控件的`setValue`方法为例：

```ts
/**
 * 设置控件值。说明，这里不进行autoTrim和最大长度的检查，应该由调用者自己检查。
 * @param v
 */
public setValue(v: string) {
	if (v === undefined || v === null) {
		v = "";
	} else {
		v = String(v); // v可能为数字
	}
	if (v !== this.value) {
		this.value = this.domBase.value = v;
	}
}
```

先比较数据后修改DOM的实践要点包括：

1. 在类成员变量上记录数据
2. 当需要修改DOM的时候，先和数据比较，如果数据和要修改的目的值不一样时才修改dom

### IDataProvider模式

`IDataProvider`模式（简称`DP`）是一种将**数据生产**和**数据消费**进行隔离的设计模式。下面这些情况往往是典型的需要考虑`DP`模式的场景：

1. 数据是异步的从服务器请求的，有可能还需要分页请求。
2. 数据的来源可能是多样的，比如维下拉框的数据（可能前端已经有了全量维表数据，可能也没有因为太大）。
3. 数据的“消费”也可能是多样的，比如可能给下拉框显示、也可能给Tree显示、也可能给ButtonGroup显示……。
4. 数据的消费过程中需要进行增删改、查询过滤等操作。

数据的生产端被定义成了一个接口：`IDataProvider`在`sys.ts`，并提供了一个默认实现`DataProvider`在`basic.ts`，实现一个`DP`时有如下实践要点：

1. 大部分情况下直接用`DataProvider`就可以了，但是也存在一些情况需要继承和扩展一个新的接口，如dtable控件就扩充了一个自己的。
2. 数据生产端不涉及任何UI的处理，应该是一个可以多处复用的数据提供类。
3. 如果需要处理国际化、格式化，应该统一在`DataProvider`中处理好。
4. DP也可以提供数据修改方法，当数据的修改方式比较多（比如有增加、有删除、有移动、有更新……）时应该使用DP来修改数据，而不是通过事件。
5. DP提供的数据可能是二维数组，可能是一维数组，DP只提供数据，不确保数据一定适配某个数据消费控件，控件应该自己适配数据格式，比如Tree就支持了`itemDataConf`构造参数用于设置如何从数据中获取需要的信息。

数据消费控件有如下实践要点：

1. 有`public setDataProvider(dataProvider: IDataProvider<any>): Promise<void>` 函数，设置新的DP对象。同时在`setDataProvider`中调用`refresh`同步更新显示，`DataProvider`一旦发生变化，显示根据数据变化同步更新。
2. 有`public refresh(): Promise<void>` 函数，表示从DP重新取数据刷新控件，可以适当的加一些参数用于控制刷新范围。并尽量确保控件的外观不发生大的闪动，例如滚动条位置不发生变化，如果数据无变化尽量不修改dom。
3. 支持构造参数`dataProvider?: IDataProvider<any>;`。
4. 如果控件本身也支持在构造参数传入静态数据，如List控件就支持`items`参数，那么内部实现的时候应该将静态数据模拟成DP，控件内部统一走DP机制。

### ICommandProvider模式 - “命令”也是“数据”

命令接口涉及到sys.ts中定义的`ICommandProvider`和`ICommandRenderer`接口，请仔细看看它们的注释，这2个接口本身也是一种将**数据生产**和**数据消费**进行隔离的设计模式。

## 参考实例

1. tree.ts - 实现了虚拟滚动、DOM复用、DP接口……
2. dwtable.ts - 实现了“模块”级别的数据与UI分离的开发模式、数据驱动UI
