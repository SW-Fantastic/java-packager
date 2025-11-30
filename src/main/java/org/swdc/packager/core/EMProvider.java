package org.swdc.packager.core;

import org.swdc.data.EMFProvider;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.entity.JavaFXEnvironment;

import java.util.List;

public class EMProvider extends EMFProvider {

    @Override
    public List<Class> registerEntities() {
        return List.of(
                JavaEnvironment.class,
                JavaFXEnvironment.class
        );
    }

}
