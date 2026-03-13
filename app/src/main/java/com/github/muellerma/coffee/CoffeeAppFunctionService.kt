package com.github.muellerma.coffee

import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.service.AppFunctionServiceDelegate
import androidx.appfunctions.internal.AggregatedAppFunctionInventory
import androidx.appfunctions.service.internal.AggregatedAppFunctionInvoker

class CoffeeAppFunctionService : AppFunctionService() {
    private val delegate by lazy {
        AppFunctionServiceDelegate(
            this,
            kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.Job(),
            appfunctions_aggregated_deps.`$AppDebug_InventoryComponentRegistry`() as AggregatedAppFunctionInventory,
            appfunctions_aggregated_deps.`$AppDebug_InvokerComponentRegistry`() as AggregatedAppFunctionInvoker,
            androidx.appfunctions.internal.NullTranslatorSelector()
        )
    }

    override suspend fun executeFunction(request: ExecuteAppFunctionRequest): ExecuteAppFunctionResponse {
        return delegate.executeFunction(request)
    }
}
