// javac Test.java && java Test
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Widget {
    protected long key = -1;

    protected Widget() {}

    public long getKey() {
        return key;
    }

    public Widget key(long key) {
        this.key = key;
        return this;
    }

    abstract public Component createComponent();

    abstract public void syncComponent(Component component);
}

class Box extends Widget {
    public static int HORIZONTAL = 0;
    public static int VERTICAL = 1;

    protected int orientation = VERTICAL;
    protected List<Widget> children;

    protected Box() {
        super();
        children = new ArrayList<Widget>();
    }

    public static Box create() {
        return new Box();
    }

    public Box orientation(int orientation) {
        this.orientation = orientation;
        return this;
    }

    public Box children(Object[] children) {
        for (Object child : children) {
            if (child instanceof Object[]) {
                children((Object[])child);
            } else if (child != null) {
                this.children.add((Widget)child);
            }
        }
        return this;
    }

    public Component createComponent() {
        JPanel panel = new JPanel();
        if (orientation == HORIZONTAL) panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        if (orientation == VERTICAL) panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    public void syncComponent(Component component) {
        JPanel panel = (JPanel)component;

        // Map<Integer, Widget> unkeyWidgets = new HashMap<Integer, Widget>();
        // Map<Long, Widget> keyedWidgets = new HashMap<Long, Widget>();

        // for (int i = 0; i < children.size(); i++) {
        //     Widget child = children.get(i);
        //     if (child.getKey() != -1) {
        //         keyedWidgets.put(child.getKey(), child);
        //     } else {
        //         unkeyWidgets.put(i, child);
        //     }
        // }

        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(i);
            Component currentComponent = null;
            try {
                currentComponent = panel.getComponent(i);
            } catch (Exception e) {}
            Component newComponent = child.createComponent();
            if (currentComponent == null) {
                currentComponent = newComponent;
                panel.add(currentComponent);
                child.syncComponent(currentComponent);
            }
            else if (currentComponent.getClass().equals(newComponent.getClass())) {
                child.syncComponent(currentComponent);
            }
            else {
                panel.remove(i);
                currentComponent = newComponent;
                panel.add(currentComponent, i);
                child.syncComponent(currentComponent);
            }
        }

        for (int i = children.size(); i < panel.getComponentCount(); i++) {
            panel.remove(i);
        }

        panel.repaint();
    }
}

class Text extends Widget {
    protected String text;

    protected Text(String text) {
        super();
        this.text = text;
    }

    public static Text create(String text) {
        return new Text(text);
    }

    public Component createComponent() {
        return new JLabel();
    }

    public void syncComponent(Component component) {
        JLabel label = (JLabel)component;
        if (!label.getText().equals(text)) {
            label.setText(text + "[" + System.identityHashCode(text) + "]");
        }
    }
}

class Button extends Text {
    protected ActionListener onClickListener;

    protected Button(String text) {
        super(text);
    }

    public static Button create(String text) {
        return new Button(text);
    }

    public Button onClick(ActionListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public Component createComponent() {
        return new JButton();
    }

    public void syncComponent(Component component) {
        JButton button = (JButton)component;
        if (!button.getText().equals(text)) {
            button.setText(text + "[" + System.identityHashCode(button) + "]");
        }
        if (onClickListener != null) {
            ActionListener[] actionListeners = button.getActionListeners();
            for (ActionListener actionListener : actionListeners) {
                button.removeActionListener(actionListener);
            }
            button.addActionListener(onClickListener);
        }
    }
}

abstract class StatelessWidget extends Widget {
    protected Widget _child;

    protected StatelessWidget() {
        super();
    }

    public abstract Widget build();

    public Component createComponent() {
        if (_child == null) _child = build();
        return _child.createComponent();
    }

    public void syncComponent(Component component) {
        if (_child == null) _child = build();
        _child.syncComponent(component);
    }
}

abstract class StatefulWidget extends StatelessWidget {
    protected Component _component;

    protected StatefulWidget() {
        super();
    }

    public void syncComponent(Component component) {
        if (_child == null) _child = build();
        _component = component;
        _child.syncComponent(component);
    }

    public void refresh() {
        _child = null;
        syncComponent(_component);
    }
}

// ####################################################################
// ####################################################################
// ####################################################################

class Item extends StatelessWidget {
    public static interface OnDeleteListener {
        public void onDelete();
    };

    protected int number;
    protected OnDeleteListener onDeleteListener;

    protected Item() {
        super();
    }

    public static Item create() {
        return new Item();
    }

    public Item number(int number) {
        this.number = number;
        return this;
    }

    public Item onDelete(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
        return this;
    }

    public Widget build() {
        return Box.create().orientation(Box.HORIZONTAL).children(new Object[] {
            Text.create("Item " + number),
            Button.create("Delete").onClick(e -> {
                if (onDeleteListener != null) {
                    onDeleteListener.onDelete();
                }
            })
        });
    }
}

class App extends StatefulWidget {
    protected List<Integer> numbers;
    protected int counter;

    protected App() {
        super();

        numbers = new ArrayList<Integer>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);

        counter = 5;
    }

    public static App create() {
        return new App();
    }

    public Widget build() {
        return Box.create().children(new Object[] {
            Text.create("Java Swing Reactive example"),

            Button.create("Add number").onClick(e -> {
                numbers.add((int)(Math.random() * 100));
                refresh();
            }),

            numbers.stream().map(number ->
                Item.create().number(number).onDelete(() -> {
                    numbers.remove(number);
                    refresh();
                }).key(number)
            ).toArray(),

            Button.create("Add number").onClick(e -> {
                numbers.add((int)(Math.random() * 100));
                refresh();
            }),

            Text.create("Counter: " + counter),
            counter > 2 ? Text.create("Counter > 2") : null,
            Box.create().orientation(Box.HORIZONTAL).children(new Object[] {
                Button.create("-").onClick(e -> {
                    counter--;
                    refresh();
                }),
                Button.create("+").onClick(e -> {
                    counter++;
                    refresh();
                }),
            }),

            Text.create("Made by Bastiaan van der Plaat")
        });
    }
}

public class Test {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Java Swing Reactive UI Experiment");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Widget root = App.create();
        Component rootComponent = root.createComponent();
        frame.add(rootComponent);
        root.syncComponent(rootComponent);
    }
}
