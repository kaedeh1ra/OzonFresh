import com.example.app.buildlogic.Constants

plugins {
    id("kmp.library")
}

// Фейки репозиториев и хранилища для тестов всех модулей. В приложение не попадает.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(Constants.Modules.DOMAIN))
            api(project(Constants.Modules.DATA))
        }
    }
}
