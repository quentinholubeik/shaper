package fr.univ.shaper.gui.model;

import fr.univ.shaper.core.GraphicBuilder;
import fr.univ.shaper.core.GraphicElement;
import fr.univ.shaper.core.element.Layer;
import fr.univ.shaper.file.Director;
import fr.univ.shaper.gui.command.Command;

import java.awt.*;

public interface DrawingBoard {

    // --------------------------------------------------- //
    //                      ACCESSEURS                     //
    // --------------------------------------------------- //

    Pencil getPencil();

    Layer getLayerRoot();

    void setLayerRoot(Layer root);

    Director getDirector();

    void setDirector(Director director);

    void setSelectedElement(GraphicElement element);

    /**
     * Renvoie l'élément graphique qui est en train d'être dessiné
     *
     * @return l'élément graphique qui est en train d'être dessiné
     *      null si rien n'est en cours de dessin.
     */
    GraphicElement getSelectedElement();

    // --------------------------------------------------- //
    //                  Avant le dessin                    //
    // --------------------------------------------------- //
    void selectGraphicElementName(String name);

    void selectGraphicElementType(String type);

    void pickColor(Color color);

    /**
     * Permet d'exécuter une commande sur la plache de dessin
     *
     * @param command à éxécuter
     */
    void run(Command command);

    // ---------------------------------------------------- //
    //                       Flags                          //
    // ---------------------------------------------------- //
    boolean canDraw();

    /**
     * Indique si c'est un nouveau dessin
     *
     * @return true si ce dessin est nouveau et n'a pas été enregistré
     */
    boolean isNew();

    /**
     * Indique si le dessin n'a pas été sauvegardé
     *
     * @return true si le dessin a été modifié et qu'il n'a pas été
     *      sauvegardé.
     */
    boolean unsaved();
}

