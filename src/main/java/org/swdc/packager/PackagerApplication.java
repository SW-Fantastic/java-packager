package org.swdc.packager;

import org.swdc.data.EMFProviderFactory;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.EnvironmentLoader;
import org.swdc.fx.FXApplication;
import org.swdc.fx.SWFXApplication;
import org.swdc.packager.core.EMProvider;
import org.swdc.packager.views.PackagerMainView;

@SWFXApplication(
        splash = SplashView.class,
        configs = PackagerConfigure.class,
        assetsFolder = "./assets",
        icons = { "icon-16.png","icon-24.png","icon-32.png","icon-64.png","icon-128.png","icon-256.png", "icon-512.png" }
)
public class PackagerApplication extends FXApplication {


    @Override
    public void onConfig(EnvironmentLoader loader) {
        loader.withProvider(EMProvider.class);
    }

    @Override
    public void onStarted(DependencyContext dependencyContext) {
        // 初始化JPA的EMF。
        EMFProviderFactory factory = dependencyContext.getByClass(EMFProviderFactory.class);
        factory.create();
        // 显示主界面。
        PackagerMainView mainView = dependencyContext.getByClass(PackagerMainView.class);
        mainView.show();
    }

}
