package koeko.Networking.OtherTransferables

import koeko.view.TransferPrefix

class ClientToServerTransferable {
    var prefix = -1
    var size = -1
    var optionalArgument1 = ""
    var fileBytes = ByteArray(0)
    
    fun getTransferableBytes() : ByteArray {
        var prefixString = prefix.toString() + TransferPrefix.delimiter + size + TransferPrefix.delimiter + optionalArgument1
        var prefixUsefulBytes = prefixString.toByteArray()
        var prefixBytes = ByteArray(TransferPrefix.prefixSize)
        for (i in 0..prefixUsefulBytes.size) {
            prefixBytes[i] = prefixUsefulBytes[i]
        }
        return prefixBytes + fileBytes
    }

    companion object {
        fun stringToTransferable(prefix: String): ClientToServerTransferable {
            val transferable = ClientToServerTransferable()
            val prefixString = prefix.split(TransferPrefix.delimiter)[0]
            val sizeString = prefix.split(TransferPrefix.delimiter)[1]
            transferable.optionalArgument1 = prefix.split(TransferPrefix.delimiter)[2]

            try {
                transferable.prefix = prefixString.toInt()
                transferable.size = sizeString.toInt()
            } catch (e: NumberFormatException) {
                println(e.message)
            }

            return transferable
        }
    }
}

object CtoSPrefix {
    const val unableToReadPrefix = -1
    const val answerPrefix = 0
    const val connectionPrefix = 1
    const val resourceIdsPrefix = 2
    const val disconnectionPrefix = 3
    const val okPrefix = 4
    const val accuserReceptionPrefix = 5
    const val activeIdPrefix = 6
    const val endTransmissionPrefix = 7
    const val hotspotIpPrefix = 8
    const val successPrefix = 9
    const val failPrefix = 10
    const val readyPrefix = 11
    const val gamesetPrefix = 12
    const val gameTeamPrefix = 13
    const val reconnectedPrefix = 14
    const val requestPrefix = 15
    const val resultPrefix = 16
}


