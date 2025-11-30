package org.swdc.packager.core.repo;

import org.swdc.data.JPARepository;
import org.swdc.data.anno.Param;
import org.swdc.data.anno.Repository;
import org.swdc.data.anno.SQLQuery;
import org.swdc.packager.core.entity.JavaEnvironment;

@Repository
public interface JavaEnvironmentRepo extends JPARepository<JavaEnvironment,Long> {

    @SQLQuery("FROM JavaEnvironment WHERE path = :path")
    JavaEnvironment findByPath(@Param("path") String path);

}
