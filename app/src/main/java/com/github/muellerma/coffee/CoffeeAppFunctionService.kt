package com.github.muellerma.coffee

import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.service.AppFunctionServiceDelegate
import androidx.appfunctions.internal.AggregatedAppFunctionInventory
import androidx.appfunctions.service.internal.AggregatedAppFunctionInvoker

class CoffeeAppFunctionService : AppFunctionService() {
    private val delegate by lazy {
        val inventoryName = "appfunctions_aggregated_deps.\$App" + if (BuildConfig.DEBUG) "Debug" else "Release" + "_InventoryComponentRegistry"
        val invokerName = "appfunctions_aggregated_deps.\$App" + if (BuildConfig.DEBUG) "Debug" else "Release" + "_InvokerComponentRegistry"

        AppFunctionServiceDelegate(
            this,
            kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.Job(),
            Class.forName(inventoryName).getDeclaredConstructor().newInstance() as AggregatedAppFunctionInventory,
            Class.forName(invokerName).getDeclaredConstructor().newInstance() as AggregatedAppFunctionInvoker,
            androidx.appfunctions.internal.NullTranslatorSelector()
        )
    }

    override suspend fun executeFunction(request: ExecuteAppFunctionRequest): ExecuteAppFunctionResponse {
        return delegate.executeFunction(request)
    }
}
