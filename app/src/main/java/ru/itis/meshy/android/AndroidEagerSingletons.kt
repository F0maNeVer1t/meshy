package ru.itis.meshy.android

/**
 * Маркер-интерфейс с одним методом для DI: позволяет компоненту провести
 * eager-инъекцию набора синглтонов через единый entry point. Внутренний
 * helper-`object` ниже инкапсулирует создание `EagerSingletons`.
 */
interface AndroidEagerSingletons {

    fun inject(init: AppModule.EagerSingletons)

    /**
     * Java-вложенный `class Helper` со `static` методом превратился в
     * Kotlin `object`. Из Java вызывается так же:
     * `AndroidEagerSingletons.Helper.injectEagerSingletons(c)`.
     */
    object Helper {

        @JvmStatic
        fun injectEagerSingletons(c: AndroidEagerSingletons) {
            c.inject(AppModule.EagerSingletons())
        }
    }
}