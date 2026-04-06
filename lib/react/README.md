# React

A lightweight declarative UI library for Android inspired by [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Litho](https://fblitho.com/). Instead of XML
layouts, you describe your UI in plain Java using composable constructor calls. On every state
change the same render code runs again and the library diffs the live view tree, reusing,
repositioning, or removing views in place with no full rebuild.

## Core concepts

### Component

`Component` is the stateful building block. Extend it, store state as fields, override `render()`
to describe the UI, and call `rebuild()` whenever state changes.

```java
public class CounterButton extends Component {
    private int count = 0;

    public CounterButton() {
        super(); // self-slots into the parent BuildContext
    }

    @Override
    protected void onMount() {
        count = 5;
        rebuild();
    }

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

Place a component inside another component's `render()` just like any other widget:

```java
new CounterButton();
```

#### Lifecycle callbacks

| Method        | When                                               |
| ------------- | -------------------------------------------------- |
| `onMount()`   | First time the component is attached to the window |
| `onUpdate()`  | Every subsequent `rebuild()` after mount           |
| `onUnmount()` | When the component is detached from the window     |

### render() rules

- `render()` is a **pure description** -- it must not have side effects beyond building the UI.
- Call `rebuild()` only from event handlers or lifecycle callbacks, never from inside `render()`.

---

## Widgets

All widgets are instantiated with `new` inside a `render()` or a children lambda. Every widget
exposes a fluent `.modifier(Modifier)` method and returns `this` for chaining.

### Text

Displays a string or string resource.

```java
new Text("Hello, world!");
new Text(R.string.greeting);
```

### Button

A standard push button with a text label.

```java
new Button("Save")
    .onClick(() -> save());

new Button(R.string.save)
    .onClick(() -> save());
```

### ImageButton

An icon button backed by a drawable resource.

```java
new ImageButton(R.drawable.ic_delete)
    .onClick(() -> delete())
    .modifier(Modifier.of().size(dp(48)));
```

### Image

A non-interactive image. Supports both drawable resources and network URLs.

```java
// Drawable resource
new Image(R.drawable.logo)
    .modifier(Modifier.of().size(dp(64)));

// Network URL -- served instantly from memory cache when available, no flash on re-render
new Image(coin.imageUrl())
    .scaleType(ImageView.ScaleType.CENTER_CROP)
    .transparent()     // use ARGB_8888 for alpha channel
    .loadingColor(ContextCompat.getColor(context, R.color.loading_background_color))
    .modifier(Modifier.of().size(dp(24)));
```

When the URL is unchanged across re-renders the `Image` is a no-op: it never clears the
existing bitmap or restarts the fetch. Only a URL change triggers a new load. If the bitmap
is already in the memory cache it is applied synchronously without any loading state.

### Spacer

An invisible view used to push siblings apart in a `Row` or `Column`.

```java
new Row(() -> {
    new Text("Left");
    new Spacer().modifier(Modifier.of().weight(1)); // fills remaining space
    new Text("Right");
});
```

### Box

A `FrameLayout`-style container -- children are stacked on top of each other.

```java
new Box(() -> {
    new Image(R.drawable.background);
    new Text("Overlay").modifier(Modifier.of().align(Gravity.CENTER));
}).modifier(Modifier.of().size(dp(200)));
```

### Row

A horizontal `LinearLayout`. Wrap content in a lambda.

```java
new Row(() -> {
    new Text("Label").modifier(Modifier.of().weight(1));
    new Button("OK");
}).modifier(Modifier.of().padding(dp(8), dp(16)));
```

Add `.scrollHorizontal()` to make it scrollable:

```java
new Row(() -> {
    for (var item : items) new ChipView(item);
}).modifier(Modifier.of().scrollHorizontal().width(matchParent()));
```

### Column

A vertical `LinearLayout`. Wrap content in a lambda.

```java
new Column(() -> {
    new Text("Title").modifier(Modifier.of().fontSize(sp(20)));
    new Text("Subtitle");
});
```

Add `.scrollVertical()` to make it scrollable:

```java
new Column(() -> {
    for (var section : sections) new SectionView(section);
}).modifier(Modifier.of().scrollVertical().weight(1).width(matchParent()));
```

### LazyColumn

A virtualized, scrollable list backed by `ListView`. Use this for lists that may be long.

```java
// Without keys -- fine for static or fully-replaced lists
new LazyColumn<>(items, item -> {
    new Text(item.name());
});

// With a header view above the list (no keys)
new LazyColumn<>(items,
    () -> new Text("Header").modifier(Modifier.of().padding(dp(16))),
    item -> new PersonItem(item));

// With a key extractor -- enables stable view recycling across dataset changes
new LazyColumn<>(items, Item::id, item -> {
    new PersonItem(item);
}).modifier(Modifier.of().weight(1).width(matchParent()));

// With a key extractor and a header view above the list
new LazyColumn<>(items, Item::id,
    () -> new Text("Header").modifier(Modifier.of().padding(dp(16))),
    item -> new PersonItem(item));
```

The key extractor accepts any type: `Long`, `Integer`, `UUID`, or anything else. `Long` and
`Integer` are mapped losslessly; `UUID` is folded with XOR; other types fall back to
`hashCode()`.

### PopupMenu

A context menu anchored to a view. Build it in an `onClick` handler and call `.show()`.

```java
new ImageButton(R.drawable.ic_more)
    .onClick(view -> new PopupMenu(view.getContext(), view)
        .item("Edit",   () -> edit())
        .item("Delete", () -> delete())
        .show());
```

String resource overload:

```java
.item(R.string.delete, () -> delete())
```

---

## Modifier

`Modifier` describes layout and visual properties. Create one with `Modifier.of()` and chain
calls. Pass it to any widget via `.modifier(...)`.

### Dimensions -- `Unit`

All size/spacing values use `Unit`. Import the static factory methods for concise usage:

```java
import static nl.plaatsoft.android.react.Unit.*;

dp(16)          // density-independent pixels
sp(16)          // scale-independent pixels (for font sizes)
px(1)           // raw pixels
matchParent()   // MATCH_PARENT
wrapContent()   // WRAP_CONTENT
```

### Size and position

```java
Modifier.of().width(matchParent())
Modifier.of().height(dp(56))
Modifier.of().size(dp(48))              // width and height equal
Modifier.of().size(dp(100), dp(50))     // width, height
Modifier.of().minWidth(dp(80))
Modifier.of().minHeight(dp(44))
Modifier.of().position(dp(12), dp(12))  // left, top (inside FrameLayout/Box)
```

### Spacing

```java
Modifier.of().padding(dp(16))                       // all sides
Modifier.of().padding(dp(8), dp(16))                // vertical, horizontal
Modifier.of().padding(dp(4), dp(8), dp(4), dp(8))   // top, right, bottom, left
Modifier.of().paddingX(dp(16))                      // left + right
Modifier.of().paddingY(dp(8))                       // top + bottom
Modifier.of().paddingTop(dp(8))
Modifier.of().paddingRight(dp(8))
Modifier.of().paddingBottom(dp(8))
Modifier.of().paddingLeft(dp(8))

Modifier.of().margin(dp(8))
Modifier.of().marginX(dp(16))
Modifier.of().marginY(dp(4))
Modifier.of().margin(dp(4), dp(8), dp(4), dp(8))    // top, right, bottom, left
Modifier.of().marginTop(dp(4))
Modifier.of().marginRight(dp(4))
Modifier.of().marginBottom(dp(4))
Modifier.of().marginLeft(dp(4))
```

### LinearLayout weight, gravity and content alignment

```java
Modifier.of().weight(1)                       // fill remaining space
Modifier.of().align(Gravity.CENTER_VERTICAL)  // position this view in its parent (layout_gravity)
Modifier.of().contentGravity(Gravity.CENTER)  // align children inside this LinearLayout (gravity)
```

`align` sets `layout_gravity` on the view's `LayoutParams` (how the view positions itself within
its parent). `contentGravity` calls `LinearLayout.setGravity()` (how children align inside this
container). They are different Android properties and can be combined.

### Background and elevation

```java
Modifier.of().background(R.drawable.card_background)
Modifier.of().background(R.color.primary_color)
Modifier.of().backgroundColor(0xFF1976D2)   // ARGB int
Modifier.of().backgroundAttr(android.R.attr.selectableItemBackground)
Modifier.of().elevation(dp(4))
```

### Scroll

```java
Modifier.of().scrollVertical()    // wraps Column in a ScrollView
Modifier.of().scrollHorizontal()  // wraps Row in a HorizontalScrollView
```

### Text properties (Text / Button only)

```java
Modifier.of().fontSize(sp(16))
Modifier.of().fontWeight(Modifier.FontWeight.NORMAL)   // 400
Modifier.of().fontWeight(Modifier.FontWeight.MEDIUM)   // 500, sans-serif-medium
Modifier.of().fontWeight(Modifier.FontWeight.BOLD)     // 700
Modifier.of().textColor(R.color.secondary_text_color)
Modifier.of().textColorInt(0xFF757575)
Modifier.of().textSingleLine()                         // ellipsize at end
Modifier.of().textGravity(Gravity.CENTER)
```

### Window insets

Apply `useWindowInsets()` to any scrollable container (`LazyColumn`, `Column` with
`.scrollVertical()`, `Row` with `.scrollHorizontal()`) to opt into edge-to-edge layout for that
view:

```java
new LazyColumn<>(items, Item::id, item -> new ItemView(item))
    .modifier(Modifier.of().weight(1).width(matchParent()).useWindowInsets());
```

**What it does:**

- The decor view gets top / left / right system-bar padding so your custom action bar and other
  non-scrolling content stay below the status bar.
- The scroll container gets `clipToPadding(false)` and bottom padding equal to the navigation
  bar height, so list items scroll _behind_ the navigation bar but are padded at rest.

**Default behavior (no `useWindowInsets()`):**

When a root `Component` attaches to a window the library automatically installs a decor listener
that applies all insets (top, left, right, _and_ bottom) to the decor view. This means layouts
without `useWindowInsets()` work correctly out of the box -- content starts below the status bar
and above the navigation bar -- with no Activity boilerplate required.

---

### Conditional rendering

Conditionally show different subtrees. The slot system reconciles by view type and position,
so switching between branches safely replaces the old views.

```java
@Override
public void render() {
    if (isLoading) {
        new Text("Loading...");
    } else {
        new ContentView(data);
    }
}
```

### Lists inside render()

Use a plain `for` loop for short, fixed-size lists that don't need virtualization.

```java
new Column(() -> {
    for (var tag : tags) {
        new Text(tag).modifier(Modifier.of().padding(dp(4)));
    }
});
```

Use `LazyColumn` when the list can be arbitrarily long.

### Nested components

A `Component` self-slots when constructed with `super()`. Its internal state (fields, mount
status) persists across parent rebuilds as long as it stays at the same slot position.

```java
@Override
public void render() {
    new Column(() -> {
        new HeaderBar();      // Component -- state is preserved across rebuilds
        new ContentArea();    // Component
    });
}
```

### Passing callbacks

Pass lambdas into composable constructors. The lambdas capture the current state at each
rebuild, so they always call back with fresh values.

```java
public class PersonItem {
    public PersonItem(Person person, Consumer<Person> onUpdate, Consumer<UUID> onDelete) {
        new Row(() -> {
            new Text(person.name());
            new Button("+").onClick(() -> onUpdate.accept(person.withAgeIncrement(1)));
            new Button("-").onClick(() -> onUpdate.accept(person.withAgeIncrement(-1)));
            new ImageButton(R.drawable.ic_delete)
                .onClick(v -> new PopupMenu(v.getContext(), v)
                    .item(R.string.delete, () -> onDelete.accept(person.id()))
                    .show());
        });
    }
}
```

### Root setup in an Activity

Add the root component to the `Activity` content view directly. No XML, no insets boilerplate
required -- the library handles system bar insets automatically.

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new HomeScreen(this));
    }
}
```

---

## How it works

Each `BuildContext` holds a reference to a parent `ViewGroup` and an index cursor. When a
widget constructor calls `c.slot(ViewType.class, creator)`:

1. If the view at the current index is already of the right type, it is **reused**.
2. Otherwise the old view is replaced and a new one is created.
3. The cursor advances.

After `render()` returns, `cleanup()` removes any views past the final cursor position (stale
views from a previous render that no longer appear in the current tree).

This means the view tree is updated **surgically** -- only views that actually change are
touched, and Android's own layout/draw system handles the rest efficiently.
