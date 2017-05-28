package net.floodlightcontroller.randomizer;

/**
 * Created by geddingsbarrineau on 4/15/17.
 */
interface IConnection <S extends Host, D extends Host> {
    void updateConnection();
    S getSource();
    D getDestination();
}
