/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.maven;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Bytes;
import org.cactoos.Input;
import org.cactoos.Text;
import org.cactoos.bytes.BytesOf;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.proc.IoCheckedBiProc;
import org.cactoos.scalar.IoChecked;
import org.cactoos.scalar.LengthOf;

/**
 * Base location for files.
 *
 * @since 0.27
 */
@SuppressWarnings("PMD.TooManyMethods")
final class Home {
    /**
     * Current working directory.
     */
    private final Path cwd;

    /**
     * BiProc with two arguments for saving {@link Input} from first argument to file from second.
     */
    private final IoCheckedBiProc<Input, Path> sve;

    /**
     * Ctor.
     *
     * @param file File
     */
    Home(final File file) {
        this(file.toPath());
    }

    /**
     * Ctor.
     *
     * @param pth Path
     */
    Home(final Path pth) {
        this.cwd = pth;
        this.sve = new IoCheckedBiProc<>(
            (input, path) -> {
                final Path target = this.resolve(path);
                if (target.toFile().getParentFile().mkdirs()) {
                    Logger.debug(this, "Directory created: %[file]s", target.getParent());
                }
                try {
                    final long bytes = new IoChecked<>(
                        new LengthOf(
                            new TeeInput(
                                input,
                                new OutputTo(target)
                            )
                        )
                    ).value();
                    Logger.debug(
                        Home.class, "File %s saved (%d bytes)",
                        target, bytes
                    );
                } catch (final IOException ex) {
                    throw new IOException(
                        String.format(
                            "Failed while trying to save to %s",
                            target
                        ),
                        ex
                    );
                }
            }
        );
    }

    /**
     * Saving string.
     *
     * @param str String
     * @param path Cwd-relative path to file
     * @throws IOException If fails
     */
    void save(final String str, final Path path) throws IOException {
        this.save(new InputOf(str), path);
    }

    /**
     * Saving text.
     *
     * @param txt Text
     * @param path Cwd-relative path to file
     * @throws IOException If fails
     */
    void save(final Text txt, final Path path) throws IOException {
        this.save(new InputOf(txt), path);
    }

    /**
     * Saving input.
     *
     * @param input Input
     * @param path Cwd-relative path to file
     * @throws IOException If fails
     * @throws IllegalArgumentException If given path is absolute
     */
    void save(final Input input, final Path path) throws IOException {
        this.sve.exec(input, path);
    }

    /**
     * Check if exists.
     *
     * @param path Cwd-relative path to file
     * @return True if exists
     * @throws IllegalArgumentException If given path is absolute
     */
    boolean exists(final Path path) {
        return Files.exists(this.resolve(path));
    }

    /**
     * Load bytes from file by path.
     *
     * @param path Cwd-relative path to file
     * @return Bytes of file
     * @throws IOException if method can't find the file by path or
     *  if some exception happens during reading the file
     * @throws IllegalArgumentException If given path is absolute
     */
    Bytes load(final Path path) throws IOException {
        return new BytesOf(Files.readAllBytes(this.resolve(path)));
    }

    /**
     * Absolute path to a file.
     *
     * @param path Cwd-relative path to file
     * @return Absolute path
     */
    Path absolute(final Path path) {
        return this.cwd.resolve(path);
    }

    /**
     * Absolute path to a file.
     *
     * @param path Relative path to file
     * @return Absolute path
     * @throws IllegalArgumentException If given path is Absolute
     */
    private Path resolve(Path path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException(
                String.format(
                    "Path must be relative to base %s but absolute given: %s",
                    this.cwd,
                    path
                )
            );
        }
        return this.absolute(path);
    }
}
