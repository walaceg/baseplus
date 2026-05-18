import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Alert, Badge, Button, Card, EmptyState, Input, Loading, Pagination, Table } from '../../shared/components/index.js';
import { useDebouncedValue } from '../../shared/hooks/useDebouncedValue.js';
import * as auditService from './auditService.js';
import './audit.css';

const DEFAULT_PAGE_SIZE = 10;

function formatTimestamp(value) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(date);
}

function getActionVariant(action) {
  switch ((action ?? '').toUpperCase()) {
    case 'CREATE':
    case 'LOGIN':
      return 'success';
    case 'UPDATE':
      return 'primary';
    case 'DELETE':
    case 'LOGOUT':
      return 'danger';
    default:
      return 'neutral';
  }
}

function toDatetimeLocalValue(isoValue) {
  if (!isoValue) {
    return '';
  }

  const date = new Date(isoValue);
  if (Number.isNaN(date.getTime())) {
    return '';
  }

  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 16);
}

function toIsoValue(localValue) {
  if (!localValue) {
    return '';
  }

  const date = new Date(localValue);
  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return date.toISOString();
}

export function AuditPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const searchValue = searchParams.get('search') ?? '';
  const dataInicialValue = searchParams.get('dataInicial') ?? '';
  const dataFinalValue = searchParams.get('dataFinal') ?? '';
  const pageValue = Number(searchParams.get('page') ?? '0');
  const sizeValue = Number(searchParams.get('size') ?? String(DEFAULT_PAGE_SIZE));
  const [searchInput, setSearchInput] = useState(searchValue);
  const [dataInicialInput, setDataInicialInput] = useState(toDatetimeLocalValue(dataInicialValue));
  const [dataFinalInput, setDataFinalInput] = useState(toDatetimeLocalValue(dataFinalValue));
  const debouncedSearch = useDebouncedValue(searchInput, 300);
  const [reloadToken, setReloadToken] = useState(0);
  const [pageData, setPageData] = useState({
    content: [],
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setSearchInput(searchValue);
  }, [searchValue]);

  useEffect(() => {
    setDataInicialInput(toDatetimeLocalValue(dataInicialValue));
  }, [dataInicialValue]);

  useEffect(() => {
    setDataFinalInput(toDatetimeLocalValue(dataFinalValue));
  }, [dataFinalValue]);

  useEffect(() => {
    if (debouncedSearch === searchValue) {
      return;
    }

    const next = new URLSearchParams(searchParams);
    const trimmed = debouncedSearch.trim();

    if (trimmed) {
      next.set('search', trimmed);
    } else {
      next.delete('search');
    }

    next.set('page', '0');
    next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    setSearchParams(next, { replace: true });
  }, [debouncedSearch, searchParams, searchValue, setSearchParams, sizeValue]);

  useEffect(() => {
    let active = true;

    async function loadAuditLogs() {
      try {
        setLoading(true);
        const data = await auditService.getAuditLogs({
          search: searchValue || undefined,
          dataInicial: dataInicialValue || undefined,
          dataFinal: dataFinalValue || undefined,
          page: Number.isFinite(pageValue) ? pageValue : 0,
          size: Number.isFinite(sizeValue) ? sizeValue : DEFAULT_PAGE_SIZE,
        });

        if (active) {
          setPageData({
            content: data.content ?? [],
            page: data.page ?? 0,
            size: data.size ?? DEFAULT_PAGE_SIZE,
            totalElements: data.totalElements ?? 0,
            totalPages: data.totalPages ?? 0,
          });
        }
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Nao foi possivel carregar a auditoria.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadAuditLogs();

    return () => {
      active = false;
    };
  }, [dataFinalValue, dataInicialValue, pageValue, reloadToken, searchValue, sizeValue]);

  function updateSearchParams(updater) {
    const next = new URLSearchParams(searchParams);
    updater(next);
    setSearchParams(next, { replace: true });
  }

  function clearFilters() {
    setSearchInput('');
    setDataInicialInput('');
    setDataFinalInput('');
    updateSearchParams((next) => {
      next.delete('search');
      next.delete('dataInicial');
      next.delete('dataFinal');
      next.set('page', '0');
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  function changePage(nextPage) {
    updateSearchParams((next) => {
      next.set('page', String(nextPage));
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  function updateDateParam(key, inputValue) {
    const isoValue = toIsoValue(inputValue);
    updateSearchParams((next) => {
      if (isoValue) {
        next.set(key, isoValue);
      } else {
        next.delete(key);
      }
      next.set('page', '0');
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  const columns = [
    {
      key: 'timestamp',
      header: 'Data/Hora',
      render: (row) => <span>{formatTimestamp(row.timestamp)}</span>,
    },
    {
      key: 'usuario',
      header: 'Usuario',
      render: (row) => <span>{row.usuario || '-'}</span>,
    },
    {
      key: 'acao',
      header: 'Acao',
      render: (row) => <Badge variant={getActionVariant(row.acao)}>{row.acao || '-'}</Badge>,
    },
    {
      key: 'entidade',
      header: 'Entidade',
      render: (row) => <span>{row.entidade || '-'}</span>,
    },
    {
      key: 'entidadeId',
      header: 'Entidade ID',
      render: (row) => <span>{row.entidadeId ?? '-'}</span>,
    },
  ];

  const hasFilters = Boolean(searchValue || dataInicialValue || dataFinalValue);

  return (
    <div className="bp-audit-page bp-list-page">
      <section className="bp-list-page__header">
        <div>
          <h1>Auditoria</h1>
          <p>Registro inicial de eventos sensiveis da Base+.</p>
        </div>
      </section>

      <Card>
        <Card.Body>
          <div className="bp-list-page__toolbar">
            <div className="bp-list-page__toolbar-row">
              <div className="bp-list-page__search">
                <Input
                  id="audit-search"
                  label="Buscar auditoria"
                  placeholder="Buscar usuario, acao ou entidade..."
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                />
              </div>
              <div className="bp-list-page__actions">
                <Button disabled={!hasFilters} size="sm" variant="secondary" onClick={clearFilters}>
                  Limpar filtros
                </Button>
              </div>
            </div>

            <div className="bp-audit-page__filters">
              <Input
                id="audit-start"
                label="Data inicial"
                type="datetime-local"
                value={dataInicialInput}
                onChange={(event) => {
                  const nextValue = event.target.value;
                  setDataInicialInput(nextValue);
                  updateDateParam('dataInicial', nextValue);
                }}
              />
              <Input
                id="audit-end"
                label="Data final"
                type="datetime-local"
                value={dataFinalInput}
                onChange={(event) => {
                  const nextValue = event.target.value;
                  setDataFinalInput(nextValue);
                  updateDateParam('dataFinal', nextValue);
                }}
              />
            </div>
          </div>
        </Card.Body>
      </Card>

      {error ? <Alert variant="error">{error}</Alert> : null}

      <Card>
        <Card.Body>
          {loading ? (
            <Loading label="Carregando auditoria..." />
          ) : pageData.content.length ? (
            <>
              <Table columns={columns} rows={pageData.content} />
              <Pagination
                page={pageData.page}
                size={pageData.size}
                totalElements={pageData.totalElements}
                totalPages={pageData.totalPages}
                onChangePage={changePage}
              />
            </>
          ) : (
            <EmptyState
              description={
                hasFilters
                  ? 'Nenhum evento corresponde aos filtros atuais. Limpe a busca para ampliar os resultados.'
                  : 'Nenhum evento de auditoria foi registrado ainda.'
              }
              title="Auditoria vazia"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
}
