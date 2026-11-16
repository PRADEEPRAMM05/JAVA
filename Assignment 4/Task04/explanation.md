# Task 04: Event-Handling Forensics (DrawPad)

## 1. Defect Identification (Task 4.1)

In the original junior developer snippet:
```java
addMouseListener(new MouseAdapter() {
    public void mouseclicked(MouseEvent e) {
        count++;
        Graphics g = getGraphics();
        g.fillOval(e.getX(), e.getY(), 6, 6);
    }
});
```

### Root Causes of Failure:
1. **Method Name Typo (`mouseclicked` vs `mouseClicked`):**
   - In Java, methods are case-sensitive. The abstract adapter class `MouseAdapter` implements `MouseListener`, providing an empty stub for `public void mouseClicked(MouseEvent e)`.
   - The developer wrote `public void mouseclicked(MouseEvent e)` (all lowercase). The Java compiler treated this as a new, overloaded, non-standard method.
   - When the user clicked, the AWT Event Dispatch Thread invoked `mouseClicked(e)` on the adapter, which executed the empty default method. The custom `mouseclicked` method was **never called**.
2. **Label Never Updated:**
   - Even if the method had run, `status.setText("Clicks: " + count)` was completely missing from the handler.
3. **Improper Painting via `getGraphics()`:**
   - Calling `getGraphics()` outside the standard AWT `paint(Graphics g)` lifecycle draws directly onto the transient native screen buffer.

---

## 2. Reproducible Failing Interaction (Task 4.2)

Even if the typo was corrected without refactoring the drawing architecture, the following interaction reproduces an immediate defect:

1. **Step 1:** The user clicks 5 times on the canvas. 5 blue dots appear on screen.
2. **Step 2:** The user minimizes the window or drags another window (e.g. browser, file manager) over the `DrawPad` frame.
3. **Step 3:** The user restores or uncovers the window.
4. **Failing Result:** **All 5 dots vanish completely**, leaving a blank window.
5. **Architectural Reason:**
   - In AWT/Swing, windows do not retain an in-memory pixel copy of their client area by default. When a window is obscured or minimized, the OS reclaims or invalidates the backing pixels.
   - Upon restore, the OS window manager posts a `PaintEvent` to the AWT EventQueue, which schedules `paint(Graphics g)`.
   - Because `getGraphics()` was used ephemerally and points were not recorded in memory, `paint(g)` runs without knowledge of past clicks and clears the canvas to background color.

---

## 3. Repaired Program Architecture (Task 4.3)

The corrected design follows the **retained-mode rendering model**:
1. Store all clicked points in an in-memory collection: `List<Point> points = new ArrayList<>()`.
2. In `mouseClicked(MouseEvent e)`:
   - Add the clicked coordinate: `points.add(new Point(e.getX(), e.getY()))`.
   - Update the status label: `status.setText("Clicks: " + points.size())`.
   - Request an asynchronous repaint: `repaint()`.
3. In `@Override public void paint(Graphics g)`:
   - Iterate through `points` and redraw every circle: `g.fillOval(p.x - 3, p.y - 3, 6, 6);`.
   - Offsetting by `radius = 3` centers the 6×6 circle directly under the mouse pointer cursor tip.

---

## 4. Compile-Time Prevention via `@Override` (Task 4.4)

When `@Override` is prepended to `mouseclicked(MouseEvent e)`:
```java
@Override
public void mouseclicked(MouseEvent e) { ... }
```
The Java compiler (`javac`) detects that no method named `mouseclicked(MouseEvent)` exists in `MouseAdapter` or `MouseListener` and rejects compilation with:
```text
DrawPad.java:23: error: method does not override or implement a method from a supertype
    @Override
    ^
1 error
```
> [!IMPORTANT]
> The `@Override` annotation transforms silent runtime bugs into immediate compile-time errors.

---

## 5. Alternative Concept and Complexity Analysis

### Comparison: Point Collection (`List<Point>`) vs. Off-Screen `BufferedImage` Double-Buffering

```text
+-----------------------------+------------------------------------+------------------------------------+
| Dimension                   | Retained List<Point> Model         | Off-Screen BufferedImage Model     |
+-----------------------------+------------------------------------+------------------------------------+
| Time per Repaint for n dots | O(n) loop drawing n circles        | O(1) bit-blit (Graphics.drawImage) |
| Memory (Space) Complexity   | O(n) points: ~24 bytes per Point   | O(width * height) fixed buffer:    |
|                             | (For 100 dots: ~2.4 KB)            | 400 * 300 * 4 bytes = ~480 KB      |
| Dynamic Scaling / Resizing  | Infinite vector fidelity           | Bitmap pixelation / scaling blur   |
| Undo / Redo Capability      | Trivial (remove last list element) | Complex (requires image snapshots) |
+-----------------------------+------------------------------------+------------------------------------+
```

**Conclusion:** For typical vector drawing and coordinate-based click tracking, storing `Point` objects in a `List<Point>` provides minimal memory consumption and clean architectural separation. For millions of continuous pen strokes, an off-screen `BufferedImage` is preferable to avoid O(n) render loop overhead.
