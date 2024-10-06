package fuzs.immersivedamageindicators.config;

public enum AnchorPoint {
    TOP_LEFT(-1, -1),
    TOP_CENTER(0, -1),
    TOP_RIGHT(1, -1),
    CENTER_LEFT(-1, 0),
    CENTER(0, 0),
    CENTER_RIGHT(1, 0),
    BOTTOM_LEFT(-1, 1),
    BOTTOM_CENTER(0, 1),
    BOTTOM_RIGHT(1, 1);

    private final int offsetX;
    private final int offsetY;

    AnchorPoint(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public boolean isLeft() {
        return this.offsetX == -1;
    }

    public boolean isCenter() {
        return this.offsetX == 0;
    }

    public boolean isRight() {
        return this.offsetX == 1;
    }

    public Positioner createPositioner(int guiWidth, int guiHeight, int elementWidth, int elementHeight) {
        return new PositionerImpl(this.offsetX, this.offsetY, guiWidth, guiHeight, elementWidth, elementHeight);
    }

    public interface Positioner {

        int getPosX(int posX);

        int getPosY(int posY);
    }

    private record PositionerImpl(int offsetX, int offsetY, int guiWidth, int guiHeight, int elementWidth, int elementHeight) implements Positioner {

        @Override
        public int getPosX(int posX) {
            return this.guiWidth / 2 + this.offsetX * (this.guiWidth / 2 - posX) - (this.offsetX + 1) * this.elementWidth / 2;
        }

        @Override
        public int getPosY(int posY) {
            return this.guiHeight / 2 + this.offsetY * (this.guiHeight / 2 - posY) - (this.offsetY + 1) * this.elementHeight / 2;
        }
    }
}
