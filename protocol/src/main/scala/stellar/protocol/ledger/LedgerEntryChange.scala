package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.int
import stellar.protocol.xdr.{Decoder, Encodable}

sealed trait LedgerEntryChange extends Encodable

case class LedgerEntryCreate(entry: LedgerEntry) extends LedgerEntryChange {
  override def encode: LazyList[Byte] = int(0) ++ entry.encode ++ int(0)
}

case class LedgerEntryUpdate(entry: LedgerEntry) extends LedgerEntryChange {
  override def encode: LazyList[Byte] = int(1) ++ entry.encode ++ int(0)
}

case class LedgerEntryDelete(entry: LedgerKey) extends LedgerEntryChange {
  override def encode: LazyList[Byte] = int(2) ++ entry.encode
}

case class LedgerEntryState(entry: LedgerEntry) extends LedgerEntryChange {
  override def encode: LazyList[Byte] = int(3) ++ entry.encode ++ int(0)
}

object LedgerEntryChange extends Decoder[LedgerEntryChange] {

  val decode: State[Seq[Byte], LedgerEntryChange] = switch[LedgerEntryChange](
    widen(LedgerEntry.decode.map(LedgerEntryCreate).flatMap(drop(int))),
    widen(LedgerEntry.decode.map(LedgerEntryUpdate).flatMap(drop(int))),
    widen(LedgerKey.decode.map(LedgerEntryDelete)),
    widen(LedgerEntry.decode.map(LedgerEntryState).flatMap(drop(int)))
  )
}
