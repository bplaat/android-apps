/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.tasks;

import android.net.Uri;

public interface Task {
    public Uri getUri();

    public boolean isCanceled();

    public boolean isFinished();

    public void cancel();

    public void finish();
}
