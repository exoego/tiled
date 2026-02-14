/*-
 * #%L
 * This file is part of libtiled-java.
 * %%
 * Copyright (C) 2004 - 2020 Thorbjørn Lindeijer <thorbjorn@lindeijer.nl>
 * Copyright (C) 2004 - 2020 Adam Turk <aturk@biggeruniverse.com>
 * Copyright (C) 2016 - 2020 Mike Thomas <mikepthomas@outlook.com>
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mapeditor.view;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import org.mapeditor.core.Map;
import org.mapeditor.core.ObjectGroup;
import org.mapeditor.core.StaggerAxis;
import org.mapeditor.core.StaggerIndex;
import org.mapeditor.core.Tile;
import org.mapeditor.core.TileLayer;

/**
 * Renderer for staggered isometric maps.
 */
public class StaggeredRenderer extends AbstractRenderer {

    private final Map map;

    /**
     * Constructor for StaggeredRenderer.
     *
     * @param map map to render
     */
    public StaggeredRenderer(Map map) {
        this.map = map;
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getMapSize() {
        final int tileWidth = map.getTileWidth();
        final int tileHeight = map.getTileHeight();
        final StaggerAxis axis = getStaggerAxis();

        if (axis == StaggerAxis.X) {
            return new Dimension(
                    map.getWidth() * (tileWidth / 2) + (tileWidth / 2),
                    map.getHeight() * tileHeight + (tileHeight / 2));
        }

        return new Dimension(
                map.getWidth() * tileWidth + (tileWidth / 2),
                map.getHeight() * (tileHeight / 2) + (tileHeight / 2));
    }

    /** {@inheritDoc} */
    @Override
    public void paintTileLayer(Graphics2D g, TileLayer layer) {
        paintLayer(g, layer, () -> {
            final int tileWidth = map.getTileWidth();
            final int tileHeight = map.getTileHeight();
            final Rectangle bounds = layer.getBounds();
            final int layerOffsetX = layer.getOffsetX() != null ? layer.getOffsetX() : 0;
            final int layerOffsetY = layer.getOffsetY() != null ? layer.getOffsetY() : 0;

            for (int y = 0; y < layer.getHeight(); ++y) {
                for (int x = 0; x < layer.getWidth(); ++x) {
                    final Tile tile = layer.getTileAt(x, y);
                    if (tile == null) {
                        continue;
                    }

                    final Image image = tile.getImage();
                    if (image == null) {
                        continue;
                    }

                    final int mapX = x + bounds.x;
                    final int mapY = y + bounds.y;
                    final Point p = tileToScreen(mapX, mapY);

                    int drawX = p.x + layerOffsetX;
                    int drawY = p.y + layerOffsetY + tileHeight - image.getHeight(null);

                    // Add offset from tileset property
                    drawX += tile.getTileSet().getTileoffset() != null ? tile.getTileSet().getTileoffset().getX() : 0;
                    drawY += tile.getTileSet().getTileoffset() != null ? tile.getTileSet().getTileoffset().getY() : 0;

                    g.drawImage(image, drawX, drawY, null);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void paintObjectGroup(Graphics2D g, ObjectGroup group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Point tileToScreen(int tileX, int tileY) {
        final int tileWidth = map.getTileWidth();
        final int tileHeight = map.getTileHeight();

        if (getStaggerAxis() == StaggerAxis.X) {
            final int x = tileX * (tileWidth / 2);
            final int y = tileY * tileHeight + (isStaggeredIndex(tileX) ? tileHeight / 2 : 0);
            return new Point(x, y);
        }

        final int x = tileX * tileWidth + (isStaggeredIndex(tileY) ? tileWidth / 2 : 0);
        final int y = tileY * (tileHeight / 2);
        return new Point(x, y);
    }

    private boolean isStaggeredIndex(int index) {
        final boolean odd = (index & 1) != 0;
        final boolean oddShifted = getStaggerIndex() == StaggerIndex.ODD;
        return odd ? oddShifted : !oddShifted;
    }

    private StaggerAxis getStaggerAxis() {
        return map.getStaggerAxis() != null ? map.getStaggerAxis() : StaggerAxis.Y;
    }

    private StaggerIndex getStaggerIndex() {
        return map.getStaggerIndex() != null ? map.getStaggerIndex() : StaggerIndex.ODD;
    }
}
