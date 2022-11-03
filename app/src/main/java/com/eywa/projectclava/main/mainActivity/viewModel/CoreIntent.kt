package com.eywa.projectclava.main.mainActivity.viewModel

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.datastore.DataStoreIntent
import com.eywa.projectclava.main.features.drawer.DrawerIntent

/**
 * Things that must be handled by the viewmodel like [DatabaseIntent], [DataStoreIntent], [MainEffect].
 * This is separate to [MainIntent] as screen's intents should map to one of these
 *
 * @see [DrawerIntent.handle]
 */
interface CoreIntent : MainIntent
