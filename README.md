# text-wrap-fit-box

![Sonatype Nexus (Release)](https://img.shields.io/nexus/r/https/oss.sonatype.org/net.kemitix/text-wrap-fit-box.svg?style=for-the-badge)
![Maven Central](https://img.shields.io/maven-central/v/net.kemitix/text-wrap-fit-box.svg?style=for-the-badge)

Word wrap text to fit a Graphics2D rectangle with options to find the optimum
font size and/or overflow into additional rectangles.

```xml
<dependency>
    <groupId>net.kemitix</groupId>
    <artifactId>text-wrap-fit-box</artifactId>
    <version>${text-wrap-fit-box.version}</version>
</dependency>
```

## Usage

### Word Wrap

Splits the input `String` into a `List` of `String`s that fits within the
`imageWidth` when drawn using the `font` onto the `graphics2D`.

```java
import net.kemitix.fit.TextFit;
import net.kemitix.fit.WordWrapper;

Font font = ...
Graphics2D graphics2D = ...
int imageWidth = ...

String inputString = ....

WordWrapper wrapper = TextFit.wrapper();
List<String> lines = wrapper.wrap(inputString, font, graphics2D, imageWidth);
```

### Box Fit

Finds the optimum `fontSize` to fill the `box` with the `inputString` when drawn
using the `graphics2D`.

```java
import net.kemitix.fit.TextFit;
import net.kemitix.fit.BoxFitter;

Function<Integer, Font> fontFactory = size -> ....
Graphics2D graphics2D = ...
Rectangle2D box = new Rectangle(....);

String inputString = ....

BoxFitter fitter = TextFit.fitter();
int fontSize = fitter.fit(inputString, fontFactory, graphics2D, box);

Font font = fontFactory.apply(fontSize);
int imageWidth = (int) box.getWidth();

WordWrapper wrapper = TextFit.wrapper();
List<String> lines = wrapper.wrap(inputString, font, graphics2D, imageWidth);
```
