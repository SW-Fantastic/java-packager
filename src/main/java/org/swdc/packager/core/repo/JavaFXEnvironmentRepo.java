package org.swdc.packager.core.repo;

import org.swdc.data.JPARepository;
import org.swdc.data.anno.Param;
import org.swdc.data.anno.Repository;
import org.swdc.data.anno.SQLQuery;
import org.swdc.packager.core.entity.JavaFXEnvironment;

@Repository
public interface JavaFXEnvironmentRepo extends JPARepository<JavaFXEnvironment,Long> {

    @SQLQuery("FROM JavaFXEnvironment WHERE path = :path")
    JavaFXEnvironment findByPath(@Param("path") String path);


}
