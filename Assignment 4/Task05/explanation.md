# Task 05: Same Screen, Different Event-Handling Styles

## 1. The Four Event-Handling Implementations (Task 5.1)

| Style | Architecture | Window Closing Handling | Scope & Field Access |
| :--- | :--- | :--- | :--- |
| **(a) Frame Implements `ActionListener`** | `public class Converter extends JFrame implements ActionListener` | `addWindowListener(new WindowAdapter() { ... })` | Direct `this` access to fields; `actionPerformed(e)` must inspect `e.getSource()` if multiple distinct actions exist. |
| **(b) Named Inner Class** | `private class ConvertHandler implements ActionListener` | `WindowAdapter` inner class | Clean encapsulation; has enclosing class reference (`Converter.this`); reusable across components in same frame. |
| **(c) Anonymous Inner Class** | `new ActionListener() { public void actionPerformed(e) { ... } }` | Anonymous `WindowAdapter` | Localized definition; generates extra bytecode file (`Converter$1.class`); verbose syntax. |
| **(d) Lambda Expression** | `ActionListener listener = e -> convert();` | `WindowAdapter` (or method ref) | Cleanest modern Java syntax; invokes `invokedynamic`; zero boilerplate class declaration. |

---

## 2. Requirement Change Impact (Task 5.2)

### Requirement Delta:
1. Trigger conversion when pressing **Enter** in `amountField`.
2. Display conversion output in a modal dialog (`JOptionPane.showMessageDialog`) in addition to updating the label.

### Change Footprint Comparison:

```text
+------------------------------+---------------------------+-----------------------------------------------+
| Style                        | Lines Changed / Added     | Modification Friction                         |
+------------------------------+---------------------------+-----------------------------------------------+
| Style A (Frame as Listener)  | ~7 lines                  | Low, but pollutes actionPerformed() with      |
|                              |                           | if (e.getSource() == amount) checks.          |
| Style B (Named Inner Class)  | ~4 lines                  | Minimal: attach existing handler instance to  |
|                              |                           | amount.addActionListener(handler).            |
| Style C (Anon Inner Class)   | ~8 lines (or duplicate)   | High if inlined: must extract to a shared     |
|                              |                           | local variable or duplicate code.             |
| Style D (Lambda Expression)  | 2 lines                   | Lowest: define shared lambda reference        |
|                              |                           | and attach to both convertBtn and amount.     |
+------------------------------+---------------------------+-----------------------------------------------+
```

---

## 3. Comparison of Event-Handling Styles (Task 5.3)

| Criterion | Style A (Frame Implements) | Style B (Named Inner Class) | Style C (Anon Inner Class) | Style D (Lambda / Method Ref) |
| :--- | :--- | :--- | :--- | :--- |
| **Readability** | Poor (spaghetti `if-else` for many buttons) | Good (descriptive class name) | Moderate (boilerplate clutter) | **Superior** (concise, direct intent) |
| **Access to Fields** | Direct (`this.field`) | Direct via enclosing scope | Direct to enclosing fields | Direct to enclosing instance methods |
| **Handler Reusability**| Within frame only | Reusable across components | Not reusable (one-shot) | Reusable within scope via variable |
| **Bytecode Overhead** | 0 extra classes | 1 extra class file (`$1.class`) | 1 extra class file per usage | **0 class files** (`invokedynamic`) |
| **Ease of Maintenance**| Low (large monolithic listener)| High (cohesive unit) | Low (hard to refactor/test) | **Very High** (delegates to methods) |

> [!TIP]
> **Architectural Recommendation for Large Applications:**
> In large enterprise desktop applications, combine **Style D (Method References / Lambdas)** for light component glue with **Swing `Action` (`AbstractAction`)** for application commands. Swing Actions centralize state (enabled/disabled), text labels, tooltips, and icons across toolbar buttons, menu items, and keyboard accelerators simultaneously.

---

## 4. Alternative Concept and Complexity Analysis

### Comparison: Lambda / Method Reference vs. Shared Swing `Action` (`AbstractAction`)

```text
+------------------------------+------------------------------------+------------------------------------+
| Dimension                    | Lambda Listener (`e -> convert()`) | Swing `Action` (`AbstractAction`)  |
+------------------------------+------------------------------------+------------------------------------+
| Space (Object Instances)     | 1 functional interface instance    | 1 `Action` object instance         |
| Shared State Management      | Requires manual enable/disable on  | Single `setEnabled(false)` flips   |
|                              | both button and text field         | button, menu item, and hotkey state|
| Multi-Component Binding      | Must attach to button, textfield,  | Pass action to `new JMenuItem(act)`|
|                              | and keymap individually           | and `new JButton(act)` natively    |
| Code Verbosity               | Minimal (~2 lines)                 | Moderate (~12 lines subclass)      |
+------------------------------+------------------------------------+------------------------------------+
```

**Verdict:** For simple screens with 1-2 controls, lambdas provide zero-ceremony clarity. For enterprise suites with Menus, Toolbars, and Keyboard Accelerators, Swing `Action` is the industry standard design pattern.
