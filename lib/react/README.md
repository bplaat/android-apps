# React

A declarative Android view library inspired by [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Litho](https://fblitho.com/). You describe your UI in Java instead of XML layouts, on every rebuild the existing view tree is diffed and reused.

## Components

Extend `Component`, store state in fields, describe the UI in `render()` and call `rebuild()` when state changes:

```java
public class Counter extends Component {
    private int count = 0;

    @Override
    public void render() {
        new Row(() -> {
            new Text("Count: " + count).modifier(Modifier.of().weight(1));
            new Button("-").onClick(() -> { count--; rebuild(); });
            new Button("+").onClick(() -> { count++; rebuild(); });
        });
    }
}
```

Nested components are created with `new Counter();` inside `render()`, their state is kept as long as they stay at the same position. Root components are set as the activity content view:

```java
setContentView(new HomeScreen(this));
```

Lifecycle callbacks: `onMount()`, `onUpdate()` (every rebuild after mount) and `onUnmount()`. Never call `rebuild()` from `render()`.

## Widgets

```java
new Text("Hello");
new Text(R.string.hello);
new Button("Save").onClick(() -> save());
new ImageButton(R.drawable.ic_delete).onClick(() -> delete());
new Image(R.drawable.logo);
new Image(url).scaleType(ImageView.ScaleType.CENTER_CROP).transparent().loadingColor(color);
new Spacer();
new Box(() -> { ... });    // FrameLayout
new Row(() -> { ... });    // horizontal LinearLayout
new Column(() -> { ... }); // vertical LinearLayout
new LazyColumn<>(items, Item::id, item -> new ItemView(item)); // ListView, key and header are optional
new PopupMenu(context, anchor).item(R.string.delete, () -> delete()).show();
```

Network images are served from the memory cache when available and are only refetched when the URL changes.

## Modifier

Every widget accepts `.modifier(Modifier.of()...)`, sizes use `Unit` (`dp()`, `sp()`, `px()`, `matchParent()`, `wrapContent()`):

- Size: `width`, `height`, `size`, `minWidth`, `minHeight`, `position`
- Spacing: `padding`, `paddingX/Y`, `paddingTop/Right/Bottom/Left` and the same for `margin`
- Layout: `weight`, `align` (layout gravity), `contentGravity` (children gravity)
- Background: `background`, `backgroundColor`, `backgroundAttr`, `elevation`
- Text: `fontSize`, `fontWeight`, `textColor`, `textColorInt`, `textSingleLine`, `textGravity`
- Scroll: `scrollVertical` (`Column`), `scrollHorizontal` (`Row`)
- Insets: `useWindowInsets` lets a scroll view draw behind the navigation bar, by default all system bar insets are applied to the decor view

## How it works

Every widget claims a slot in the current `BuildContext`, when the view at that position has the same type it is reused, otherwise it is replaced. After `render()` all leftover views are removed.
