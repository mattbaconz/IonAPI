# Animated Items

Create items that cycle through textures or states automatically.

### Usage

```java
import com.ionapi.gui.AnimatedItem;

// Create an animated item
AnimatedItem loading = AnimatedItem.create()
    .frame(IonItem.of(Material.RED_WOOL, "<red>Loading."))
    .frame(IonItem.of(Material.YELLOW_WOOL, "<yellow>Loading.."))
    .frame(IonItem.of(Material.LIME_WOOL, "<green>Loading..."))
    .interval(5) // Change every 5 ticks
    .loop(true)
    .build();

// Add to GUI
gui.setAnimatedItem(13, loading);
```

### Handling Completion

You can run code when an animation finishes (if not looping):

```java
AnimatedItem countdown = AnimatedItem.create()
    .frame(item3)
    .frame(item2)
    .frame(item1)
    .loop(false)
    .onComplete(anim -> {
        player.sendMessage("Go!");
        gui.close(player);
    })
    .build();
```
