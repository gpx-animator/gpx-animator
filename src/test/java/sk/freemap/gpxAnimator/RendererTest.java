package sk.freemap.gpxAnimator;

import manifold.ext.api.Jailbreak;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RendererTest {

    private @Jailbreak Renderer r= new Renderer(Configuration.createBuilder().build());

    @Mock
    private Point2D p = mock(Point2D.class, RETURNS_SMART_NULLS);

    @Mock
    private Graphics2D g = mock(Graphics2D.class, RETURNS_SMART_NULLS);;

    @Test
    public void testDrawSimpleCircleOnGraphics2DRendererNullInputThrowsException() throws UserException{
        assertThrows(NullPointerException.class, () -> {
        r.drawSimpleCircleOnGraphics2D(null,null);});
    }

    @Test
    public void testDrawIconOnGraphics2DRendererNullInputThrowsException() throws UserException{
        assertThrows(NullPointerException.class, () -> {
            r.drawIconOnGraphics2D(null,null);});
    }

    @Test
    public void testDrawIconOnGraphics2DUsesCoordinates() throws UserException{
        r.drawIconOnGraphics2D(p,g);
        verify(p,atLeastOnce()).getX();
        verify(p,atLeastOnce()).getY();
        verify(g,atLeastOnce()).drawImage(any(Image.class),any(AffineTransform.class), ArgumentMatchers.isNull());
    }

    @Test
    public void testDrawIconOnGraphics2DDrawsImage() throws UserException{
        r.drawIconOnGraphics2D(p,g);
        verify(g,atLeastOnce()).drawImage(any(Image.class),any(AffineTransform.class), ArgumentMatchers.isNull());
    }

    @Test
    public void testDrawSimpleCircleOnGraphics2DUsesCoordinates() throws UserException{
        r.drawSimpleCircleOnGraphics2D(p,g);
        verify(p,atLeastOnce()).getX();
        verify(p,atLeastOnce()).getY();
    }

    @Test
    public void testDrawSimpleCircleOnGraphics2DModifiesGraphicAsIntended() throws UserException{
        r.drawSimpleCircleOnGraphics2D(p,g);
        verify(g,atLeastOnce()).setStroke(any(BasicStroke.class));
        verify(g,atLeastOnce()).fill(any(Ellipse2D.Double.class));
        verify(g,atLeastOnce()).setColor(any(Color.class));
        verify(g,atLeastOnce()).draw(any(Ellipse2D.Double.class));
    }


}