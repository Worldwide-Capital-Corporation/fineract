read -p "[?] Are you sure you want to remove all Vault's data (y/n)? " answer
case ${answer:0:1} in
    y|Y )
        echo "[*] Removing files..."
        echo "[+] Removing: ./vault/data/consul/"
        rm -rf ./vaultdata/consul/
        echo "[+] Removing: ./vault/data/backup/"
		rm -rf ./vault/data/backup/
		echo "[+] Removing: ./vault/data/keys.txt"
		rm -f ./vault/data/keys.txt
    ;;
    * )
        echo "[*] Aborting..."
    ;;
esac
