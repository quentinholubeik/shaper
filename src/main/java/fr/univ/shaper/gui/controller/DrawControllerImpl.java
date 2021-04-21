package fr.univ.shaper.gui.controller;

import fr.univ.shaper.core.GraphicBuilder;
import fr.univ.shaper.core.GraphicElement;
import fr.univ.shaper.core.GraphicFactoryHandler;
import fr.univ.shaper.core.element.Layer;
import fr.univ.shaper.core.element.Point;
import fr.univ.shaper.core.element.noisy.NoisyGraphicFactory;
import fr.univ.shaper.core.exception.BadGraphicContextException;
import fr.univ.shaper.core.exception.GraphicTypeNotFoundException;
import fr.univ.shaper.file.Director;
import fr.univ.shaper.file.FileType;
import fr.univ.shaper.gui.model.Pencil;
import fr.univ.shaper.util.Contract;
import fr.univ.shaper.file.xml.DirectorXML;
import fr.univ.shaper.core.GraphicStateListener;
import fr.univ.shaper.visitor.PrintGraphicVisitor;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;

public class DrawControllerImpl implements DrawController {

    private final Layer rootGraphicElement;

    private final GraphicBuilder builder;

    private GraphicStateListener graphicStateListener;

    private GraphicElement draggedElement;

    private final Pencil pencil;

    private Director director;

    public DrawControllerImpl(GraphicBuilder builder) {
        Contract.assertThat(builder != null, "Le builder ne doit pas être null");
        try {
            NoisyGraphicFactory config =(NoisyGraphicFactory) GraphicFactoryHandler
                    .newInstance().getFactoryOf("Noisy");
            config.setGenerateNoise(true);
        } catch (GraphicTypeNotFoundException e) {
            e.printStackTrace();
        }
        this.builder = builder;
        rootGraphicElement = new Layer();
        pencil = new Pencil();
    }

    @Override
    public void selectGraphicElementName(String name) {
        Contract.assertThat(name != null, "Le paramètre name ne doit pas être null");
        pencil.setShapeName(name);
        pickColor(Color.BLACK);
    }

    @Override
    public void selectGraphicElementType(String type) {
        Contract.assertThat(type != null, "Le paramètre type ne doit pas être null");
        Contract.assertThat(canDraw(), "Vous devez selectionner une forme avant de dessiner");
        pencil.setShapeType(type);
    }

    @Override
    public void pickColor(Color color) {
        Contract.assertThat(color != null, "Le paramètre color ne doit pas être null");
        pencil.setColor(color);
    }

    @Override
    public void startDrawingPosition(Point2D point) {
        Contract.assertThat(point != null, "Le paramètre point ne doit pas être null");
        System.out.println("START : "+ point);
        pencil.putPencil(point);
    }

    @Override
    public void computeDragEndDropper(Point2D point) {
        Contract.assertThat(point != null, "Le paramètre point ne doit pas être null");
        Contract.assertThat(pencil.isDrawing(), "Le crayon n'est pas en train de dessiner");

        pencil.upPencil(point);
        draggedElement = buildGraphicElement();;
    }

    @Override
    public GraphicElement getDraggedElement() {
        return draggedElement;
    }

    @Override
    public void endDrawingPosition(Point2D point) {
        Contract.assertThat(point != null, "Le paramètre point ne doit pas être null");
        Contract.assertThat(pencil.isDrawing(), "Le crayon n'est pas en train de dessiner");
        System.out.println("END : "+ point);

        pencil.upPencil(point);
        GraphicElement element = buildGraphicElement();


        if (element != null) {
            rootGraphicElement.append(element);
            graphicStateListener.event(element);
            element.accept(new PrintGraphicVisitor());
        }

        draggedElement = null;
        pencil.reset();
    }

    @Override
    public boolean canDraw() {
        return pencil != null && pencil.canDraw();
    }

    @Override
    public void saveDrawing(FileType format, File file) {
        Contract.assertThat(format != null, "Le paramètre format ne doit pas être null");
        Contract.assertThat(file != null, "Le paramètre filename ne doit pas être null");

        if (director == null) {
            if (format == FileType.XML) {
                director = new DirectorXML(builder);
                director.saveAs(file, rootGraphicElement);
            }
        }

        // TODO test si director exist pour saveAs ou save !
    }

    @Override
    public void loadDrawing(FileType format, File file) {
        Contract.assertThat(format != null, "Le paramètre format ne doit pas être null");
        Contract.assertThat(file != null, "Le paramètre filename ne doit pas être null");

        if (format == FileType.XML) {
            director = new DirectorXML(builder);
            GraphicElement ge = director.load(file);
            ge.accept(new PrintGraphicVisitor());
            graphicStateListener.event(ge);
        }
    }

    @Override
    public void addDrawingListener(GraphicStateListener listener) {
        Contract.assertThat(listener != null, "L'écouteur ne doit pas être égal à null");
        graphicStateListener = listener;
    }

    // ---------------------------------------------------------- //
    //                          TOOLS                             //
    // ---------------------------------------------------------- //

    private GraphicElement buildGraphicElement() {
        Contract.assertThat(pencil.getEndPoint() != null,
                "Le paramètre point ne doit pas être null");
        Contract.assertThat(pencil.isDrawing(), "Le crayon n'est pas en train de dessiner");

        builder.setGraphicName(pencil.getShapeName())
                .setGraphicType(pencil.getShapeType())
                .setGraphicAttribute("color", pencil.getColor(), Color.class)
                .appendPoint(new Point(
                        pencil.getStartPoint().getX(),
                        pencil.getStartPoint().getY()
                        )
                );

        if (pencil.hasRadius()) {
            builder.setGraphicAttribute("radius", pencil.getDistance(), double.class);
        } else {
            builder.appendPoint(new Point(
                    pencil.getEndPoint().getX(),
                    pencil.getEndPoint().getY()
                    )
            );
        }

        GraphicElement element = null;

        try {
            element = builder.build();
        } catch (BadGraphicContextException e) {
            e.printStackTrace();
        }

        return element;
    }
}
