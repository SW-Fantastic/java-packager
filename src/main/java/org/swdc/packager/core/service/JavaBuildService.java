package org.swdc.packager.core.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.swdc.data.StatelessHelper;
import org.swdc.data.anno.Transactional;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.entity.JavaFXEnvironment;
import org.swdc.packager.core.repo.JavaEnvironmentRepo;
import org.swdc.packager.core.repo.JavaFXEnvironmentRepo;
import org.swdc.packager.views.event.JavaEnvRefreshEvent;
import org.swdc.packager.views.event.JavaFXEnvRefreshEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class JavaBuildService implements EventEmitter {

    @Inject
    private JavaEnvironmentRepo repository;

    @Inject
    private JavaFXEnvironmentRepo fxRepository;

    private Events events;

    @Transactional
    public JavaEnvironment saveJavaEnvironment(JavaEnvironment environment) {
        JavaEnvironment exist = repository.findByPath(environment.getPath());
        if (exist == null) {
            exist = new JavaEnvironment();
        }
        exist.setPath(environment.getPath());
        exist.setVersion(environment.getVersion());
        exist = repository.save(exist);
        emit(new JavaEnvRefreshEvent());
        return StatelessHelper.stateless(exist);
    }

    @Transactional
    public void removeJavaEnvironment(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        JavaEnvironment environment = repository.getOne(id);
        if (environment == null) {
            return;
        }
        repository.remove(environment);
        emit(new JavaEnvRefreshEvent());
    }

    @Transactional
    public List<JavaEnvironment> getAllJavaEnvironments() {
        List<JavaEnvironment> environments = repository.getAll();
        if (environments == null) {
            return new ArrayList<>();
        }
        return environments.stream()
                .map(StatelessHelper::stateless)
                .collect(Collectors.toList());
    }


    @Transactional
    public JavaFXEnvironment saveJavaFXEnvironment(JavaFXEnvironment environment) {
        JavaFXEnvironment exist = fxRepository.findByPath(environment.getPath());
        if (exist == null) {
            exist = new JavaFXEnvironment();
        }
        exist.setPath(environment.getPath());
        exist.setName(environment.getName());
        exist = fxRepository.save(exist);
        emit(new JavaFXEnvRefreshEvent());
        return StatelessHelper.stateless(exist);
    }

    @Transactional
    public void removeJavaFXEnvironment(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        JavaFXEnvironment environment = fxRepository.getOne(id);
        if (environment == null) {
            return;
        }
        emit(new JavaFXEnvRefreshEvent());
        fxRepository.remove(environment);
    }

    @Transactional
    public List<JavaFXEnvironment> getAllJavaFXEnvironments() {
        List<JavaFXEnvironment> environments = fxRepository.getAll();
        if (environments == null) {
            return new ArrayList<>();
        }
        return environments.stream()
                .map(StatelessHelper::stateless)
                .collect(Collectors.toList());
    }


    @Override
    public <T extends AbstractEvent> void emit(T t) {
        events.dispatch(t);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
