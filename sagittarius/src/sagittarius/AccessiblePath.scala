package sagittarius

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

opaque type CPath <: Path = Path

object CPath:
  def apply(p: Path): Either[Throwable, CPath] =
    val dir = p.getParent
    if Files.isExecutable(dir) && Files.isWritable(dir) && !Files.exists(p) then
      Right(p)
    else Left(new Error(s"Cannot create files in $dir"))

opaque type WPath <: Path = Path

object WPath:
  def apply(p: Path): Either[Throwable, WPath] =
    if Files.isWritable(p) then Right(p)
    else Left(new Error(s"Cannot write to $p"))

opaque type CWPath <: CPath = Path

object CWPath:
  def apply(p: Path): Either[Throwable, CWPath] =
    CPath(p).orElse(WPath(p))

opaque type EPath <: Path = Path

object EPath:
  def apply(p: Path): Either[Throwable, EPath] =
    if Files.exists(p) then Right(p)
    else Left(new Error(s"Can't read from $p"))

opaque type RPath <: EPath = Path

object RPath:
  def apply(p: Path): Either[Throwable, RPath] =
    if Files.isReadable(p) then Right(p)
    else
      Left(
        new Error(s"Can't read from $p")
      )

opaque type RWPath <: RPath = Path

object RWPath:
  def apply(p: Path): Either[Throwable, RWPath] =
    RPath(p).filterOrElse(
      p =>
        Files
          .isWritable(p) || (!Files.exists(p) && Files.isWritable(p.getParent)),
      new Error(s"Can't write to $p")
    )
