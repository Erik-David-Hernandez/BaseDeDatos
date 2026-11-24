#!/usr/bin/env bash
# Install a local PostgreSQL server and load the project's database once.
set -euo pipefail

fail() { printf 'Error: %s\n' "$*" >&2; exit 1; }
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
sql_file="$script_dir/biblioteca_virtual.sql"
db_name=Biblioteca_virtual
app_mode=false
[[ -r "$sql_file" ]] || fail "SQL file not found: $sql_file"
[[ ${1:-} != --help ]] || {
    printf 'Usage: bash database/setup-postgresql.sh [--app]\n--app also provisions the app login and database permissions.\nSupports Debian/Ubuntu, Arch/CachyOS, macOS (Homebrew), and Windows (Git Bash + winget).\n'
    exit 0
}
if [[ $# == 1 && $1 == --app ]]; then app_mode=true;
elif [[ $# != 0 ]]; then fail 'Use --help for usage.'; fi

as_root() {
    if [[ $EUID == 0 ]]; then "$@"; else sudo "$@"; fi
}
as_postgres() {
    if [[ $EUID == 0 ]]; then runuser -u postgres -- "$@";
    else sudo -u postgres -- "$@"; fi
}

case $(uname -s) in
    Linux)
        [[ -r /etc/os-release ]] || fail 'Cannot identify Linux distribution.'
        . /etc/os-release
        case " ${ID:-} ${ID_LIKE:-} " in
            *debian*|*ubuntu*)
                if ! command -v psql >/dev/null || ! command -v pg_lsclusters >/dev/null; then
                    as_root apt-get update
                    as_root apt-get install -y postgresql postgresql-contrib
                fi
                # pg_lsclusters is also the server check: psql alone is only a client.
                if [[ -z $(pg_lsclusters --no-header) ]]; then
                    version=$(ls /usr/lib/postgresql | sort -V | tail -n 1)
                    [[ -n "$version" ]] || fail 'No PostgreSQL server installed.'
                    as_root pg_createcluster "$version" main --start
                fi
                while read -r version cluster port status rest; do
                    if [[ $port == 5432 ]]; then
                        as_root pg_ctlcluster "$version" "$cluster" start ||
                            [[ $status == online ]] || fail 'Could not start PostgreSQL.'
                        break
                    fi
                done < <(pg_lsclusters --no-header)
                ;;
            *arch*|*cachyos*)
                if ! command -v psql >/dev/null || ! command -v initdb >/dev/null; then
                    as_root pacman -Syu --needed --noconfirm postgresql
                fi
                if [[ ! -f /var/lib/postgres/data/PG_VERSION ]]; then
                    as_postgres initdb -D /var/lib/postgres/data --auth-local=peer --auth-host=scram-sha-256
                fi
                as_root systemctl start postgresql
                ;;
            *) fail "Unsupported Linux distribution: ${ID:-unknown}. Install PostgreSQL manually." ;;
        esac
        client=(as_postgres psql -h /var/run/postgresql -p 5432 -U postgres)
        ;;
    Darwin)
        [[ $EUID != 0 ]] || fail 'Run this script as your normal user on macOS.'
        command -v brew >/dev/null || fail 'Install Homebrew first: https://brew.sh'
        # Use an existing Homebrew server if available; do not replace its version.
        formula=$(brew list --formula | awk '/^postgresql(@[0-9]+)?$/ {print; exit}')
        if [[ -z "$formula" ]]; then
            brew install postgresql@17
            formula=postgresql@17
        fi
        export PATH="$(brew --prefix "$formula")/bin:$PATH"
        brew services start "$formula"
        client=(psql -h localhost -p 5432 -U "$(id -un)")
        ;;
    MINGW*|MSYS*|CYGWIN*)
        command -v powershell.exe >/dev/null || fail 'Use Git Bash with Windows PowerShell available.'
        find_windows_psql() {
            powershell.exe -NoProfile -Command '$ErrorActionPreference = "Stop"; Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\psql.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName' | tr -d '\r'
        }
        windows_psql=$(find_windows_psql)
        if [[ -z "$windows_psql" ]]; then
            command -v winget.exe >/dev/null || fail 'Install App Installer (winget) from the Microsoft Store first.'
            printf 'Complete the PostgreSQL installer, keeping port 5432. Remember the postgres password.\n'
            winget.exe install --exact --id PostgreSQL.PostgreSQL.17 --source winget --interactive --accept-source-agreements --accept-package-agreements
            windows_psql=$(find_windows_psql)
        fi
        [[ -n "$windows_psql" ]] || fail 'PostgreSQL installation did not provide psql.'
        # Starting an existing service may require Git Bash to run as administrator.
        powershell.exe -NoProfile -Command 'Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue | Where-Object Status -ne "Running" | Start-Service -ErrorAction Stop' ||
            fail 'Start the PostgreSQL service in Windows Services, then rerun.'
        if [[ -z ${PGPASSWORD:-} ]]; then
            read -r -s -p 'PostgreSQL postgres password: ' PGPASSWORD
            printf '\n'
            export PGPASSWORD
        fi
        client=("$(cygpath -u "$windows_psql")" -h localhost -p 5432 -U postgres)
        ;;
    *) fail "Unsupported OS: $(uname -s)" ;;
esac

# Ignore user psql configuration and stop at the first SQL error.
psql_admin() { "${client[@]}" -X -v ON_ERROR_STOP=1 "$@"; }
ready=false
for ((attempt=0; attempt<15; attempt++)); do
    if psql_admin -w -d postgres -Atqc 'SELECT 1' >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 1
done
[[ $ready == true ]] || fail 'Cannot connect to PostgreSQL on port 5432. Check the service and credentials (PGPASSWORD/.pgpass).'

if [[ $app_mode == true ]]; then
    export BIBLIOTECA_DB_USER=${BIBLIOTECA_DB_USER:-biblioteca}
    export BIBLIOTECA_DB_PASSWORD=${BIBLIOTECA_DB_PASSWORD:-admin}
    # Read secrets from the environment, never process arguments or command output.
    # On Linux sudo normally removes them, so send the psql input via stdin instead.
    # psql variables are quoted explicitly as SQL identifiers/literals.
    app_user_b64=$(printf '%s' "$BIBLIOTECA_DB_USER" | base64 | tr -d '\r\n')
    app_password_b64=$(printf '%s' "$BIBLIOTECA_DB_PASSWORD" | base64 | tr -d '\r\n')
    psql_admin -w -d postgres >/dev/null <<SQL
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L',
    convert_from(decode('$app_user_b64', 'base64'), 'UTF8'),
    convert_from(decode('$app_password_b64', 'base64'), 'UTF8'))
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname =
    convert_from(decode('$app_user_b64', 'base64'), 'UTF8'))
\gexec
SQL
fi

grant_app_access() {
    [[ $app_mode == true ]] || return 0
    psql_admin -w -d "$db_name" >/dev/null <<SQL
SELECT format('GRANT CONNECT ON DATABASE %I TO %I', current_database(),
    convert_from(decode('$app_user_b64', 'base64'), 'UTF8'))
\gexec
SELECT format('GRANT USAGE ON SCHEMA public TO %I',
    convert_from(decode('$app_user_b64', 'base64'), 'UTF8'))
\gexec
SELECT format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO %I',
    convert_from(decode('$app_user_b64', 'base64'), 'UTF8'))
\gexec
SELECT format('GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO %I',
    convert_from(decode('$app_user_b64', 'base64'), 'UTF8'))
\gexec
SQL
}

exists=$(psql_admin -w -d postgres -Atqc "SELECT 1 FROM pg_database WHERE datname = '$db_name'")
if [[ $exists == 1 ]]; then
    grant_app_access
    printf 'Database %s already exists; skipping initialization.\n' "$db_name"
    exit 0
fi

# The first two lines create/connect to the database for standalone psql use.
# Manage them here so the schema and seed data can be imported in one transaction.
[[ $(head -n 1 "$sql_file") == 'CREATE DATABASE "Biblioteca_virtual";' ]] || fail 'Unexpected SQL database header.'
psql_admin -w -d postgres -c 'CREATE DATABASE "Biblioteca_virtual";'
if ! tail -n +3 "$sql_file" | psql_admin -w -d "$db_name" --single-transaction; then
    # Only remove the database created by this invocation; the import rolled back.
    psql_admin -w -d postgres -c 'DROP DATABASE "Biblioteca_virtual";' ||
        printf 'Cleanup failed. Remove the newly created empty database before retrying.\n' >&2
    fail 'Database import failed; see SQL error above.'
fi
grant_app_access
printf 'Initialized %s from %s.\n' "$db_name" "$sql_file"
